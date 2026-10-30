import dev.ultraboost.chunks.ChunkPreparationPlanner;
import dev.ultraboost.chunks.ChunkPreparationPlanner.Candidate;
import dev.ultraboost.chunks.ChunkPreparationPlanner.ChunkPos;

import java.util.List;

public final class ChunkPreparationPlannerSelfTest {
    public static void main(String[] args) {
        prioritisesDirection();
        enforcesHardBound();
        ignoresUnknownAndDuplicatePositionsSafely();
        handlesZeroDirection();
        avoidsCoordinateOverflow();
        System.out.println("ChunkPreparationPlannerSelfTest: OK");
    }

    private static void prioritisesDirection() {
        List<Candidate> result = ChunkPreparationPlanner.plan(
                List.of(new ChunkPos(0, 3), new ChunkPos(3, 0), new ChunkPos(-3, 0)),
                0, 0, 1, 0, 8, 8);
        check(!result.isEmpty() && result.get(0).pos().equals(new ChunkPos(3, 0)),
                "chunk in look direction should rank first");
        check(result.stream().noneMatch(c -> c.pos().equals(new ChunkPos(-3, 0))),
                "chunk directly behind should be excluded");
    }

    private static void enforcesHardBound() {
        List<ChunkPos> known = new java.util.ArrayList<>();
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) known.add(new ChunkPos(x, z));
        }
        List<Candidate> result = ChunkPreparationPlanner.plan(known, 0, 0, 0, 1, 8, 4);
        check(result.size() <= 4, "planner must respect hard candidate bound");
    }

    private static void ignoresUnknownAndDuplicatePositionsSafely() {
        List<Candidate> result = ChunkPreparationPlanner.plan(
                java.util.Arrays.asList(new ChunkPos(1, 0), null, new ChunkPos(1, 0)),
                0, 0, 1, 0, 4, 10);
        check(result.size() == 1, "nulls and duplicate positions must not leak into result");
    }

    private static void handlesZeroDirection() {
        List<Candidate> result = ChunkPreparationPlanner.plan(
                List.of(new ChunkPos(1, 0), new ChunkPos(0, 2)),
                0, 0, 0, 0, 4, 10);
        check(result.size() == 2, "zero direction should fall back to distance priority");
    }

    private static void avoidsCoordinateOverflow() {
        List<Candidate> result = ChunkPreparationPlanner.plan(
                List.of(new ChunkPos(Integer.MAX_VALUE, Integer.MAX_VALUE)),
                Integer.MIN_VALUE, Integer.MIN_VALUE, 1, 0, 8, 8);
        check(result.isEmpty(), "far coordinates must not overflow into the search radius");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
