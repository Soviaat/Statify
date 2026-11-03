package dev.soviaat.utils;

import dev.soviaat.Common;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class WorldSettingsManager {
    public static class WorldSettings {
        public boolean milestones = true;
        public boolean last_session_notifications = true;
    }

    private static File getSettingsFile(String worldName) {
        File dir = new File("Statify/" + worldName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "settings.json");
    }

    public static WorldSettings loadSettings(String worldName) {
        File file = getSettingsFile(worldName);
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                WorldSettings settings = Common.gson.fromJson(reader, WorldSettings.class);
                return settings != null ? settings : new WorldSettings();
            } catch (IOException e) {
                Common.LOGGER.error("Failed to load settings for world: {}", worldName, e);
            }
        }
        return new WorldSettings();
    }

    public static void saveSettings(String worldName, WorldSettings settings) {
        File file = getSettingsFile(worldName);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            Common.gson.toJson(settings, writer);
            Common.LOGGER.info("Saved settings for world: {}", worldName);
        } catch (IOException e) {
            Common.LOGGER.error("Failed to save settings for world: {}", worldName, e);
        }
    }
}
