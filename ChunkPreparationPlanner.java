package dev.ultraboost.chunks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pure, bounded planner for prioritising preparation of chunks that the client
 * already knows about. It does NOT request, generate, load, or unload chunks.
 * The game integration must supply only chunks already present in the client cache.
 */
public final class ChunkPreparationPlanner {
    private static final double MIN_DIRECTION_DOT = -0.15;
    private static final int MAX_INPUT_CHUNKS = 4096;

    private ChunkPreparationPlanner() {}

    public record ChunkPos(int x, int z) {}

    public record Candidate(ChunkPos pos, double priority, double distanceSquared) {}

    /**
     * Returns at most maxCandidates known chunks, ordered from highest to lowest
     * preparation priority. Direction components are normalised internally.
     *
     * @param knownClientChunks chunk positions already available to the client
     * @param playerChunkX current player chunk X
     * @param playerChunkZ current player chunk Z
     * @param directionX horizontal look direction X
     * @param directionZ horizontal look direction Z
     * @param maxRadiusChunks maximum search radius, in chunk columns
     * @param maxCandidates hard upper bound for returned work
     */
    public static List<Candidate> plan(
            Collection<ChunkPos> knownClientChunks,
            int playerChunkX,
            int playerChunkZ,
            double directionX,
            double directionZ,
            int maxRadiusChunks,
            int maxCandidates) {
        if (knownClientChunks == null || knownClientChunks.isEmpty()
                || maxRadiusChunks <= 0 || maxCandidates <= 0) {
            return List.of();
        }

        double length = Math.hypot(directionX, directionZ);
        if (!Double.isFinite(length) || length < 1.0e-6) {
            directionX = 0.0;
            directionZ = 0.0;
            length = 0.0;
        } else {
            directionX /= length;
            directionZ /= length;
        }

        long radiusSquared = (long) maxRadiusChunks * maxRadiusChunks;
        Set<ChunkPos> unique = new HashSet<>(Math.min(knownClientChunks.size(), MAX_INPUT_CHUNKS));
        List<Candidate> candidates = new ArrayList<>();
        int inspected = 0;

        for (ChunkPos pos : knownClientChunks) {
            if (inspected >= MAX_INPUT_CHUNKS) break;
            if (pos == null || !unique.add(pos)) continue;
            inspected++;
            long dx = (long) pos.x() - playerChunkX;
            long dz = (long) pos.z() - playerChunkZ;
            if (Math.abs(dx) > maxRadiusChunks || Math.abs(dz) > maxRadiusChunks) continue;
            long distanceSquared = dx * dx + dz * dz;
            if (distanceSquared == 0L || distanceSquared > radiusSquared) continue;

            double distance = Math.sqrt(distanceSquared);
            double alignment = (dx * directionX + dz * directionZ) / distance;
            if (length != 0.0 && alignment < MIN_DIRECTION_DOT) continue;

            // Prefer chunks ahead, then nearby chunks. Keep scores bounded.
            double directionalBonus = length == 0.0 ? 0.0 : Math.max(0.0, alignment) * 2.0;
            double priority = directionalBonus + 1.0 / (1.0 + distance);
            candidates.add(new Candidate(pos, priority, distanceSquared));
        }

        candidates.sort(Comparator
                .comparingDouble(Candidate::priority).reversed()
                .thenComparingDouble(Candidate::distanceSquared)
                .thenComparingInt(c -> c.pos().x())
                .thenComparingInt(c -> c.pos().z()));

        if (candidates.size() > maxCandidates) {
            return List.copyOf(candidates.subList(0, maxCandidates));
        }
        return List.copyOf(candidates);
    }
}
