package dev.soviaat;

import dev.soviaat.commands.CountDays;
import dev.soviaat.commands.CommandManager;
import dev.soviaat.utils.UploadManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

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
			CommandManager.register(dispatcher, registryAccess, environment);
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
							putDayCount((int) (world.getTimeOfDay() / 24000L));
							writeDaysToFile(worldName, getDayCountAsString());
							try {
								UpdateStatsFromCSV(dayFilePath, "Raw_Data!I3", worldName);
							} catch (IOException | GeneralSecurityException e) {
								LOGGER.error("Failed to upload CSV days to Google Spreadsheets", e);
							}
						}

						String csvFilePath = "Statify/" + worldName + "/statify_stats.csv";
						try {
							UpdateStatsFromCSV(csvFilePath, "Raw_Data!A1", worldName);
						} catch (IOException | GeneralSecurityException e) {
							LOGGER.error("Failed to upload CSV data to Google Spreadsheets", e);
						}
					}
				}
		});
	}
}



