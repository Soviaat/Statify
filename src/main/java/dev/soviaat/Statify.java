package dev.soviaat;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;


import static dev.soviaat.Common.*;
import static dev.soviaat.Common.worldStatusMap;
import static dev.soviaat.FileManagement.*;

public class Statify implements ModInitializer {

	public long lastUpdateTime = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Statify is being initialized...");

		loadWorldStatus();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher, environment);
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			String worldName = world.getServer().getSaveProperties().getLevelName();
			if(!"on".equals(worldStatusMap.getOrDefault(worldName, "off"))) return;

			long currentTime = world.getTime();

			if (currentTime - lastUpdateTime > 600) {
				lastUpdateTime = currentTime;
				for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
					writeStatsToFile(player, worldName);
				}
			}
		});
	}

	private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(CommandManager.literal("statify")
				.then(CommandManager.literal("enable")
						.executes(context -> {
							MinecraftServer server = context.getSource().getServer();
							String worldName = server.getOverworld().getServer().getSaveProperties().getLevelName();

							String currentStatus = worldStatusMap.getOrDefault(worldName, "off");

							if ("on".equals(currentStatus)) {
								context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID)  + "]§r Collection has already been §benabled§r for this world."));
							} else {
								context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID)  + "]§r Stat collection enabled for world: §l" + worldName));
								putWorldStatus(worldName, "on");
								saveWorldStatus();
							}
							return 1;
						})
				)
				.then(CommandManager.literal("disable")
						.executes(context -> {
							MinecraftServer server = context.getSource().getServer();
							String worldName = server.getOverworld().getServer().getSaveProperties().getLevelName();

							String currentStatus = worldStatusMap.getOrDefault(worldName, "off");
							if("off".equals(currentStatus)) {
							context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Collection has already been §bdisabled§r for this world."));
							} else {
								context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Stat collection disabled for world: §l" + worldName));
								putWorldStatus(worldName, "off");
								saveWorldStatus();
							}

							return 1;
						})
				)
		);
	}

}



