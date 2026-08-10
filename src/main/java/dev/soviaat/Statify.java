package dev.soviaat;

import dev.soviaat.commands.CommandManagement;
import dev.soviaat.commands.CountDays;
import dev.soviaat.commands.SheetId;
import dev.soviaat.utils.GoogleSheetsUtil;
import dev.soviaat.utils.UploadManager;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class Statify implements ModInitializer {
	private static UploadManager uploadManager;
	public static long lastUpdateTime = 0L;

	public static UploadManager getUploadManager() {
		return uploadManager;
	}

	@Override
	public void onInitialize() {
		Common.LOGGER.info("Statify is being initialized...");
		uploadManager = new UploadManager();
		FileManagement.loadWorldStatus();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CommandManagement.register(dispatcher, registryAccess, environment);
			CountDays.register(dispatcher, registryAccess, environment);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (ServerLevel world : server.getAllLevels()) {
				String worldName = server.getWorldData().getLevelName();
				if ("on".equals(Common.worldStatusMap.getOrDefault(worldName, "off"))) {
					long worldTime = world.getGameTime();

					for (ServerPlayer player : world.players()) {
						CompletableFuture.runAsync(() -> {
							FileManagement.writeStatsToFile(player, worldName);
							Common.LOGGER.info("Uploading stats since player is leaving the world.");
							if (uploadManager.isWorldUploading(worldName)) {
								String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
								GoogleSheetsUtil.updateStatsFromCSV(csvFilePath, "Raw_Data!A1", worldName);
								this.uploadToSheetsAsync(worldName, worldTime);
							}
						});
					}
				}
			}
		});

		ServerTickEvents.END_LEVEL_TICK.register(world -> {
			String worldName = world.getServer().getWorldData().getLevelName();
			boolean isUploading = uploadManager.isWorldUploading(worldName);
			if ("on".equals(Common.worldStatusMap.getOrDefault(worldName, "off"))) {
				long currentTime = Math.abs(world.getGameTime());
				if (currentTime - lastUpdateTime >= 2400L) {
					lastUpdateTime = currentTime;

					for (ServerPlayer player : world.players()) {
						FileManagement.writeStatsToFile(player, worldName);
					}

					if (isUploading) {
						this.uploadToSheetsAsync(worldName, currentTime);
					}

					if ((long)Common.getDayCount(worldName) < currentTime / 24000L) {
						Common.putDayCount((int)(currentTime / 24000L));
						FileManagement.writeDaysToFile(worldName, Common.getDayCountAsString());
						if (isUploading) {
							String dayFilePath = "Statify/" + worldName + "/days.csv";
							CompletableFuture.runAsync(() -> {
								try {
									GoogleSheetsUtil.updateStatsFromCSV(dayFilePath, "Raw_Data!I3", worldName);
								} catch (Exception e) {
									Common.LOGGER.error("Failed to upload days.csv to Google Sheets", e);
								}
							});
						}
					}
				}
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			String worldName = server.getWorldData().getLevelName();
			FileManagement.savePlayerName(player.getName().getString(), worldName);

			Component startMsg = Component.literal("Statify started");
			player.sendSystemMessage(startMsg, true);

			if ("on".equals(Common.worldStatusMap.getOrDefault(worldName, "off")) && uploadManager.isWorldUploading(worldName)) {
				CompletableFuture.runAsync(() -> {
					String sheetId = FileManagement.loadSheetIdFromJson(worldName);
					if (!SheetId.checkSheetVersion(worldName)) {
						player.sendSystemMessage(SheetId.sendOutdatedSheetMessage());
						FileManagement.removeSheetIdFromJson(worldName, sheetId);
						Common.LOGGER.warn(SheetId.checkSheetVersion(worldName)
								? "Sheet version matches, sending nothing to the player."
								: "Sheet version does not match, sending warning to the player.");
					}
				});
			}
		});

		ServerLevelEvents.LOAD.register((server, world) -> {
			String worldName = server.getWorldData().getLevelName();
			if ("on".equals(Common.worldStatusMap.getOrDefault(worldName, "off"))) {
				CompletableFuture.runAsync(() -> {
					try {
						if (uploadManager.isWorldUploading(worldName)) {
							String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
							Common.LOGGER.info("Updating stats because player is joining.");
							GoogleSheetsUtil.updateStatsFromCSV(csvFilePath, "Raw_Data!A1", worldName);
						}
					} catch (Exception e) {
						Common.LOGGER.error("Failed to update stats on world start.", e);
					}
				});
			}
		});
	}

	private void uploadToSheetsAsync(String worldName, long timeOfDay) {
		CompletableFuture.runAsync(() -> {
			try {
				String dayFilePath = "Statify/" + worldName + "/days.csv";
				if ((long)Common.getDayCount(worldName) < timeOfDay / 24000L) {
					Common.putDayCount((int)(timeOfDay / 24000L));
					FileManagement.writeDaysToFile(worldName, Common.getDayCountAsString());
					GoogleSheetsUtil.updateStatsFromCSV(dayFilePath, "Raw_Data!I3", worldName);
				}

				String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
				GoogleSheetsUtil.updateStatsFromCSV(csvFilePath, "Raw_Data!A1", worldName);
			} catch (Exception e) {
				Common.LOGGER.error("Failed to upload CSV data to Google Spreadsheets", e);
			}
		});
	}
}