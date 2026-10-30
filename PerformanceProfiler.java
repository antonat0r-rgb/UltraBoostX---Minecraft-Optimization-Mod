package dev.ultraboost.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Low-overhead observer: it measures frame pacing and never changes game state. */
public final class PerformanceProfiler {
    private static final Logger LOGGER = LoggerFactory.getLogger("UltraBoost X / Profiler");
    private static final int CAPACITY = 1200;
    private static final long REPORT_INTERVAL_NS = 5_000_000_000L;
    private static final long MAX_VALID_FRAME_NS = 2_000_000_000L;
    private static final FrameTimeWindow WINDOW = new FrameTimeWindow(CAPACITY);

    private static long previousFrameNs;
    private static long reportStartNs;
    private static volatile String lastReport = "No profiler report yet. Wait at least five seconds in-game.";
    private static volatile boolean enabled = true;

    private PerformanceProfiler() {}

    public static void onFrame(RenderFrameEvent.Post event) {
        if (!enabled) return;
        long now = System.nanoTime();
        if (previousFrameNs != 0L) {
            long delta = now - previousFrameNs;
            // Ignore pauses, window switches and debugger stalls.
            if (delta > 0L && delta < MAX_VALID_FRAME_NS) WINDOW.add(delta);
        }
        previousFrameNs = now;
        if (reportStartNs == 0L) reportStartNs = now;
        if (now - reportStartNs >= REPORT_INTERVAL_NS) {
            publishReport();
            reportStartNs = now;
        }
    }

    private static void publishReport() {
        FrameTimeWindow.Snapshot s = WINDOW.snapshot();
        if (s.count() < 2) return;
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / 1_048_576L;
        long committed = runtime.totalMemory() / 1_048_576L;
        long max = runtime.maxMemory() / 1_048_576L;
        lastReport = String.format(
                java.util.Locale.ROOT,
                "avg %.1f FPS | frame %.2f ms avg | p95 %.2f ms | p99 %.2f ms | range %.2f..%.2f ms | samples %d | heap %d/%d/%d MiB used/committed/max",
                s.averageFps(), s.averageFrameMs(), s.p95Ns() / 1_000_000.0,
                s.p99Ns() / 1_000_000.0, s.minNs() / 1_000_000.0,
                s.maxNs() / 1_000_000.0, s.count(), used, committed, max);
        LOGGER.info("5s sample: {}", lastReport);
        WINDOW.clear();
    }

    public static String getLastReport() { return lastReport; }
    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean value) {
        enabled = value;
        previousFrameNs = 0L;
        reportStartNs = 0L;
        WINDOW.clear();
    }

    public static void reset() {
        previousFrameNs = 0L;
        reportStartNs = 0L;
        WINDOW.clear();
        lastReport = "Profiler reset. Wait at least five seconds in-game.";
    }
}
