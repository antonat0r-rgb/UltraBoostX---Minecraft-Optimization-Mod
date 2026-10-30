package dev.ultraboost;

import dev.ultraboost.client.ClientCommands;
import dev.ultraboost.client.PerformanceProfiler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client-only entry point. No mixins, renderer replacement, or server hooks. */
@Mod(value = UltraBoost.MOD_ID, dist = Dist.CLIENT)
public final class UltraBoost {
    public static final String MOD_ID = "ultraboost";
    private static final Logger LOGGER = LoggerFactory.getLogger("UltraBoost X");

    public UltraBoost() {
        Runtime runtime = Runtime.getRuntime();
        LOGGER.info("UltraBoost X {} initialized on physical client", "0.4.1");
        LOGGER.info("Runtime: {} logical processors; heap max={} MiB",
                runtime.availableProcessors(), runtime.maxMemory() / 1_048_576L);
        LOGGER.info("Sodium detected: {}", ModList.get().isLoaded("sodium"));
        NeoForge.EVENT_BUS.addListener(PerformanceProfiler::onFrame);
        NeoForge.EVENT_BUS.addListener(ClientCommands::register);
        LOGGER.info("Profiler ready. Client commands: /ubx, /ubx report, /ubx reset, /ubx on, /ubx off.");
        LOGGER.info("This build measures frame pacing; it does not yet claim to increase FPS.");
    }
}
