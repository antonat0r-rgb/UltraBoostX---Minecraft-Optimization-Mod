package dev.ultraboost.client;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/** Local-only diagnostic commands; these are never sent to a multiplayer server. */
public final class ClientCommands {
    private ClientCommands() {}

    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ubx")
                .executes(context -> status(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> status(context.getSource())))
                .then(Commands.literal("report")
                        .executes(context -> {
                            context.getSource().sendSystemMessage(Component.literal(
                                    "[UltraBoost X] " + PerformanceProfiler.getLastReport()));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("reset")
                        .executes(context -> {
                            PerformanceProfiler.reset();
                            context.getSource().sendSystemMessage(Component.literal(
                                    "[UltraBoost X] Profiler samples reset."));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("on")
                        .executes(context -> toggle(context.getSource(), true)))
                .then(Commands.literal("off")
                        .executes(context -> toggle(context.getSource(), false))));
    }

    private static int status(net.minecraft.commands.CommandSourceStack source) {
        source.sendSystemMessage(Component.literal("[UltraBoost X] Profiler: "
                + (PerformanceProfiler.isEnabled() ? "ON" : "OFF")
                + " | Sodium detected: " + net.neoforged.fml.ModList.get().isLoaded("sodium")));
        source.sendSystemMessage(Component.literal("Use /ubx report, /ubx reset, /ubx on, /ubx off."));
        return Command.SINGLE_SUCCESS;
    }

    private static int toggle(net.minecraft.commands.CommandSourceStack source, boolean value) {
        PerformanceProfiler.setEnabled(value);
        source.sendSystemMessage(Component.literal("[UltraBoost X] Profiler "
                + (value ? "enabled." : "disabled.")));
        return Command.SINGLE_SUCCESS;
    }
}
