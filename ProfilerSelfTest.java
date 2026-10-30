import dev.ultraboost.client.FrameTimeWindow;

public final class ProfilerSelfTest {
    public static void main(String[] args) {
        FrameTimeWindow w = new FrameTimeWindow(3);
        w.add(10_000_000L);
        w.add(20_000_000L);
        w.add(30_000_000L);
        var first = w.snapshot();
        check(first.count() == 3, "three samples");
        check(Math.abs(first.averageFps() - 50.0) < 0.001, "average FPS");
        w.add(40_000_000L); // oldest sample (10ms) must be evicted
        var second = w.snapshot();
        check(second.count() == 3, "ring stays bounded");
        check(second.minNs() == 20_000_000L, "oldest sample evicted");
        check(second.maxNs() == 40_000_000L, "newest sample retained");
        w.clear();
        check(w.size() == 0, "clear");
        System.out.println("FrameTimeWindow self-test: PASS");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
