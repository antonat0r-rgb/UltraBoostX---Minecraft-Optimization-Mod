package dev.ultraboost.client;

import java.util.Arrays;

/**
 * Fixed-capacity frame-time ring buffer. No allocation occurs when adding a sample.
 * This class intentionally has no Minecraft/NeoForge dependencies so its logic
 * can be tested independently from the game runtime.
 */
public final class FrameTimeWindow {
    private final long[] samples;
    private int size;
    private int next;

    public FrameTimeWindow(int capacity) {
        if (capacity < 2) throw new IllegalArgumentException("capacity must be >= 2");
        this.samples = new long[capacity];
    }

    public void add(long frameTimeNs) {
        if (frameTimeNs <= 0L) return;
        samples[next] = frameTimeNs;
        next = (next + 1) % samples.length;
        if (size < samples.length) size++;
    }

    public int size() { return size; }
    public int capacity() { return samples.length; }

    public Snapshot snapshot() {
        if (size == 0) return Snapshot.empty();
        long[] ordered = new long[size];
        long total = 0L;
        long min = Long.MAX_VALUE;
        long max = 0L;
        int start = size == samples.length ? next : 0;
        for (int i = 0; i < size; i++) {
            long value = samples[(start + i) % samples.length];
            ordered[i] = value;
            total += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        long[] sorted = ordered.clone();
        Arrays.sort(sorted);
        return new Snapshot(size, total, min, max,
                percentile(sorted, 0.95), percentile(sorted, 0.99));
    }

    public void clear() {
        Arrays.fill(samples, 0L);
        size = 0;
        next = 0;
    }

    private static long percentile(long[] sorted, double p) {
        int index = (int) Math.ceil(p * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
    }

    public record Snapshot(int count, long totalNs, long minNs, long maxNs,
                           long p95Ns, long p99Ns) {
        static Snapshot empty() { return new Snapshot(0, 0, 0, 0, 0, 0); }
        public double averageFrameMs() {
            return count == 0 ? 0.0 : totalNs / (double) count / 1_000_000.0;
        }
        public double averageFps() {
            return totalNs <= 0L ? 0.0 : count * 1_000_000_000.0 / totalNs;
        }
    }
}
