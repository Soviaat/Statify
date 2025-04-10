package dev.soviaat;

import dev.soviaat.commands.CountDays;
import dev.soviaat.commands.CommandManager;
import dev.soviaat.dashboard.DashboardPrompt;
import dev.soviaat.dashboard.DashboardServer;
import dev.soviaat.dashboard.IdGenerator;
import dev.soviaat.utils.UploadManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.telemetry.WorldLoadedEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static dev.soviaat.Common.*;
import static dev.soviaat.Common.worldStatusMap;
import static dev.soviaat.FileManagement.*;
import static dev.soviaat.dashboard.IdGenerator.generateId;
import static dev.soviaat.commands.SheetId.checkSheetVersion;
import static dev.soviaat.commands.SheetId.sendOutdatedSheetMessage;
import static dev.soviaat.utils.GoogleSheetsUtil.UpdateStatsFromCSV;

public class Statify implements ModInitializer {
	private UploadManager uploadManager;
	public static long lastUpdateTime = 0;

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
			long timeOfDay = world.getTimeOfDay();
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
					if (getDayCount(worldName) < timeOfDay / 24000L) {
						LOGGER.info("Day count:" + getDayCountAsString() + " | Time of day:" + timeOfDay / 24000L);

						putDayCount((int) (timeOfDay / 24000L));
						writeDaysToFile(worldName, getDayCountAsString());
					}
				}

			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();

			if(player != null) {
				String worldName = player.getWorld().getServer().getSaveProperties().getLevelName();

				FileManagement.savePlayerName(player.getName().toString(), worldName);
				String sheetId = FileManagement.loadSheetIdFromJson(worldName);

				if("on".equals(worldStatusMap.getOrDefault(worldName, "off")) && uploadManager.isWorldUploading(worldName)) {
					boolean	versionMatches = checkSheetVersion(worldName);
					if (versionMatches) {
						return;
					} else {
						player.sendMessage(sendOutdatedSheetMessage(), false);
						FileManagement.removeSheetIdFromJson(worldName, sheetId);
					}
					LOGGER.warn(versionMatches ? "Sheet version matches, sending nothing to the player." : "Sheet version does not match, sending warning to the player.");
				}
			}
		});

		ServerWorldEvents.LOAD.register((server, world) -> {
			String worldName = server.getSaveProperties().getLevelName();

			Path filePath = Paths.get("Statify", worldName, "statify-id.txt");
			File idFile = filePath.toFile();
			File idParentDir = idFile.getParentFile();
			if(!idParentDir.exists()) {
				idParentDir.mkdirs();
			}

			String uniqueID = generateId();
			try (FileWriter writer = new FileWriter(idFile)) {
				writer.write(uniqueID);
			} catch (IOException e) {
				LOGGER.error("Failed to write unique ID to file", e);
			}

			if(!"on".equals(worldStatusMap.getOrDefault(worldName, "off"))) {
				LOGGER.info("World upload is not enabled for: " + worldName);
				return;
			}
			synchronized (this) {
				if (uploadManager.isWorldUploading(worldName)) {
					String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
					try {
						LOGGER.info("Updating stats because player is joining.");
						UpdateStatsFromCSV(csvFilePath, "Raw_Data!A1", worldName);
					} catch (IOException | GeneralSecurityException e) {
						LOGGER.error("Failed to update stats on world start.", e);
					}
				}
			}
		});

//		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
//			DashboardServer.stopDashboardServer();
//		});
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



