package dev.soviaat.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.soviaat.Common;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class CountDays {
    public static int dayTime;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("days").executes(CountDays::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        long totalTicks = level.dimensionType().defaultClock()
                .map(clock -> level.clockManager().getTotalTicks(clock))
                .orElseGet(() -> {
                    ServerLevel overworld = level.getServer().overworld();
                    return overworld.dimensionType().defaultClock()
                            .map(clock -> overworld.clockManager().getTotalTicks(clock))
                            .orElse(0L);
                });

        // so this is really infuriating now. Remember, this was level.getLevelData().getGameTime() in earlier versions. so like two lines of code turned into 8, how cool. fuck me.

        dayTime = (int) (totalTicks / 24000L);

        source.sendSuccess(() -> Component.literal("Day count: " + dayTime + " days"), false);
        return 1;
    }

}

