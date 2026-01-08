package dev.soviaat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.soviaat.Common;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class UploadManager {
    private static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
    private static final String UPLOAD_FILE = "config/statify/current_upload.json";
    private String currentUploadWorld;

    public UploadManager() {
        this.loadCurrentUploadWorld();
    }

    private CompletableFuture<Void> loadCurrentUploadWorld() {
        return CompletableFuture.runAsync(() -> {
            File configFile = new File("config/statify/current_upload.json");
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    JsonObject json = (JsonObject)gson.fromJson(reader, JsonObject.class);
                    this.currentUploadWorld = json.has("currentUploadWorld") ? json.get("currentUploadWorld").getAsString() : null;
                    Common.LOGGER.info("Loaded current upload world: {}", this.currentUploadWorld);
                } catch (IOException e) {
                    Common.LOGGER.error("Failed to load current upload world from Json", e);
                }
            } else {
                this.currentUploadWorld = null;
            }

        }).whenComplete((result, ex) -> {
            if (ex != null) {
                Common.LOGGER.error("Failed to load current upload world", ex);
            }

        });
    }

    private void saveUploadWorld() {
        CompletableFuture.runAsync(() -> {
            File configFile = new File("config/statify/current_upload.json");
            configFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(configFile)) {
                JsonObject json = new JsonObject();
                json.addProperty("currentUploadWorld", this.currentUploadWorld);
                gson.toJson(json, writer);
                Common.LOGGER.info("Saved upload world: {}", this.currentUploadWorld);
            } catch (IOException e) {
                Common.LOGGER.error("Failed to save upload world to Json", e);
            }

        });
    }

    public CompletableFuture<String> getUploadWorldAsync() {
        return this.loadCurrentUploadWorld().thenApply((v) -> this.currentUploadWorld);
    }

    public String getUploadWorld() {
        return this.currentUploadWorld;
    }

    public void setUploadWorld(String worldName) {
        this.currentUploadWorld = worldName;
        this.saveUploadWorld();
    }

    public void clearUploadWorld() {
        this.currentUploadWorld = null;
        this.saveUploadWorld();
    }

    public boolean isWorldUploading(String worldName) {
        return worldName.equals(this.currentUploadWorld);
    }
}
