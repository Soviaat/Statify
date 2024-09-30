package dev.soviaat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.mojang.text2speech.Narrator.LOGGER;

public class UploadManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String UPLOAD_FILE = "config/statify/current_upload.json";

    private String currentUploadWorld;

    public UploadManager() {
        loadCurrentUploadWorld();
    }

    private void loadCurrentUploadWorld() {
        CompletableFuture.runAsync(() -> {
            File configFile = new File(UPLOAD_FILE);

            if(configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    JsonObject json = gson.fromJson(reader, JsonObject.class);
                    currentUploadWorld = json.has("currentUploadWorld") ? json.get("currentUploadWorld").getAsString() : null;
                    LOGGER.info("Loaded current upload world: {}", currentUploadWorld);
                } catch (IOException e) {
                    LOGGER.error("Failed to load current upload world from Json", e);
                }
            } else {
                currentUploadWorld = null;
            }
        });
    }

    private void saveUploadWorld() {
        CompletableFuture.runAsync(() -> {
            File configFile = new File(UPLOAD_FILE);
            configFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(configFile)) {
                JsonObject json = new JsonObject();
                json.addProperty("currentUploadWorld", currentUploadWorld);
                gson.toJson(json, writer);
                LOGGER.info("Saved upload world: {}", currentUploadWorld);
            } catch (IOException e) {
                LOGGER.error("Failed to save upload world to Json", e);
            }
        });
    }

    public String getUploadWorld() {
        return currentUploadWorld;
    }

    public void setUploadWorld(String worldName) {
        currentUploadWorld = worldName;
        saveUploadWorld();
    }

    public void clearUploadWorld() {
        currentUploadWorld = null;
        saveUploadWorld();
    }

    public boolean isWorldUploading(String worldName) {
        return worldName.equals(currentUploadWorld);
    }
}
