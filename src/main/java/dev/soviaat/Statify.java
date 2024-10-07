package dev.soviaat;

import dev.soviaat.commands.CountDays;
import dev.soviaat.commands.CommandManager;
import dev.soviaat.utils.UploadManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.CompletableFuture;

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
			CommandManager.register(dispatcher, registryAccess, environment);
			CountDays.register(dispatcher, registryAccess, environment);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				String worldName = world.getServer().getSaveProperties().getLevelName();

				if(!"on".equals(worldStatusMap.getOrDefault(worldName, "off"))) {
					LOGGER.info("World upload is not enabled for: " + worldName);
					continue;
				}

				long worldTime = world.getTimeOfDay();
				for(ServerPlayerEntity player : world.getPlayers()) {
					synchronized (this) {
						writeStatsToFile(player, worldName);
						LOGGER.info("Uploading stats since player is leaving the world.");

						if(uploadManager.isWorldUploading(worldName)) {
							String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
							try {
								UpdateStatsFromCSV(csvFilePath, "Raw_Data!A1", worldName);
								uploadToSheetsAsync(worldName, worldTime);
							} catch (IOException | GeneralSecurityException e) {
								LOGGER.error("Failed to upload stats on player exit.", e);
							}
						}
					}
				}

			}
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
				String worldName = world.getServer().getSaveProperties().getLevelName();
				if(!"on".equals(worldStatusMap.getOrDefault(worldName, "off"))) return;

				long currentTime = world.getTimeOfDay();
				synchronized (this) {
					if (currentTime - lastUpdateTime >= 2400) {
						lastUpdateTime = currentTime;

						for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
							writeStatsToFile(player, worldName);
						}

						if(uploadManager.isWorldUploading(worldName)) {
							uploadToSheetsAsync(worldName, world.getTimeOfDay());
						}
					}
				}
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			for(ServerWorld world : server.getWorlds()) {

				String worldName = world.getServer().getSaveProperties().getLevelName();

				if(!"on".equals(worldStatusMap.getOrDefault(worldName, "off"))) {
					LOGGER.info("World upload is not enabled for: " + worldName);
					continue;
				}
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					synchronized (this) {
						if(uploadManager.isWorldUploading(worldName)) {
							String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
							try {
								LOGGER.info("Updating stats because player is joining.");
								UpdateStatsFromCSV(csvFilePath, "Raw_Data!A1", worldName);
							} catch (IOException | GeneralSecurityException e) {
								LOGGER.error("Failed to update stats on world start.", e);
							}
						}
					}
				}
			}
		});
	}

	private void uploadToSheetsAsync(String worldName, long timeOfDay) {
		CompletableFuture.runAsync(() -> {
			try {
				String dayFilePath = "Statify/" + worldName + "/days.csv";
				if (getDayCount(worldName) < timeOfDay / 24000L) {
					putDayCount((int) (timeOfDay / 24000L));
					writeDaysToFile(worldName, getDayCountAsString());
					UpdateStatsFromCSV(dayFilePath, "Raw_Data!I3", worldName);
				}
				String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
				UpdateStatsFromCSV(csvFilePath, "Raw_Data!A1", worldName);
			} catch (IOException | GeneralSecurityException e) {
				LOGGER.error("Failed to upload CSV data to Google Spreadsheets", e);
			}
		});
	}
}



