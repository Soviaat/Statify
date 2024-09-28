package dev.soviaat;

import com.mojang.brigadier.CommandDispatcher;
import dev.soviaat.utils.UploadManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static dev.soviaat.Common.*;
import static dev.soviaat.Common.worldStatusMap;
import static dev.soviaat.FileManagement.*;
import static dev.soviaat.utils.GoogleSheetsUtil.UpdateStatsFromCSV;

public class Statify implements ModInitializer {
	private UploadManager uploadManager;
	public long lastUpdateTime = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Statify is being initialized...");
		uploadManager = new UploadManager();
		loadWorldStatus();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher, environment);
			CountDays.register(dispatcher, registryAccess, environment);
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			String worldName = world.getServer().getSaveProperties().getLevelName();
			if(!"on".equals(worldStatusMap.getOrDefault(worldName, "off"))) return;

			long currentTime = world.getTime();

			if (currentTime - lastUpdateTime > 2400) {
				lastUpdateTime = currentTime;
				for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
					writeStatsToFile(player, worldName);
				}

				if(uploadManager.isWorldUploading(worldName)) {
					String dayFilePath = "Statify/" + worldName + "/days.csv";
					if(getDayCount(worldName) < world.getTimeOfDay() / 24000L) {
						try {
							putDayCount((int) (world.getTimeOfDay() / 24000L));
							writeDaysToFile(worldName, getDayCountAsString());
							UpdateStatsFromCSV(dayFilePath, "Raw_Data!I3");
						} catch (IOException | GeneralSecurityException e) {
							LOGGER.error("Failed to upload CSV days to Google Spreadsheets", e);
						}
					}

					String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
					try {
						UpdateStatsFromCSV(csvFilePath, "Raw_Data!A1");
					} catch (IOException | GeneralSecurityException e) {
						LOGGER.error("Failed to upload CSV data to Google Spreadsheets", e);
					}
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
				.then(CommandManager.literal("upload_on")
						.executes(context -> {
							MinecraftServer server = context.getSource().getServer();
							String worldName = server.getOverworld().getServer().getSaveProperties().getLevelName();

							String currentUploadWorld = uploadManager.getUploadWorld();

							if (currentUploadWorld != null && currentUploadWorld.equals(worldName)) {
								context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload for this world is already §benabled§r for this world."));
							} else {
								if (currentUploadWorld != null) {
									context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload has been disabled for §b" + currentUploadWorld + "§r."));
								}

								uploadManager.setUploadWorld(worldName);
								context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload has been §benabled§r for this world. (" + worldName + ")"));
							}

							return 1;
						})
				)
				.then(CommandManager.literal("upload_off")
						.executes(context -> {
							MinecraftServer server = context.getSource().getServer();
							String worldName = server.getOverworld().getServer().getSaveProperties().getLevelName();

							if(uploadManager.isWorldUploading(worldName)) {
								uploadManager.clearUploadWorld();
								context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload has been §bdisabled§r for this world. (" + worldName + ")"));
							} else {
								context.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload is §balready disabled§r for this world."));
							}

							return 1;
						})
				)
		);
	}
}



