package dev.soviaat.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class CountDays {
    public static int dayTime;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register((LiteralArgumentBuilder<ServerCommandSource>) CommandManager.literal("days").executes(CountDays::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerWorld world = source.getWorld();
        dayTime = (int) (world.getTimeOfDay() / 24000L);
        source.sendFeedback(() -> Text.literal("Day count: " + dayTime + " days"), false);
        return 1;
    }
}
