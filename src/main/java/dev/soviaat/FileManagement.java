package dev.soviaat;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.soviaat.achievements.AchievementData;
import dev.soviaat.sessiontracker.SessionData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil.GSON;

public class FileManagement {
    static Map<String, StatType<?>> statCategories = new LinkedHashMap<>();

    private static boolean createParentDirs(File file) {
        File parentDir = file.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            Common.LOGGER.error("Failed to create directories: {}", parentDir.getAbsolutePath());
            return false;
        } else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, List<SavedCoord>> loadCoordsFromJson() {
        File coordsFile = new File("Statify/savedCoords.json");
        if (!coordsFile.exists()) {
            return new LinkedHashMap<>();
        }
        try (FileReader reader = new FileReader(coordsFile, StandardCharsets.UTF_8)) {
            Type type = (new TypeToken<Map<String, List<SavedCoord>>>() {}).getType();
            Map<String, List<SavedCoord>> data = (Map<String, List<SavedCoord>>) Common.gson.fromJson(reader, type);
            return data != null ? data : new LinkedHashMap<>();
        } catch (IOException e) {
            Common.LOGGER.error("Failed to load saved coordinates from JSON", e);
            return new LinkedHashMap<>();
        }
    }

    public static void saveCoord(String worldName, SavedCoord newCoord) {
        try {
            Map<String, List<SavedCoord>> data = loadCoordsFromJson();
            List<SavedCoord> worldCoords = data.computeIfAbsent(worldName, k -> new ArrayList<>());

            worldCoords.removeIf(c -> c.getName().equalsIgnoreCase(newCoord.getName()));
            worldCoords.add(newCoord);

            File coordsFile = new File("Statify/savedCoords.json");
            if (createParentDirs(coordsFile)) {
                try (FileWriter writer = new FileWriter(coordsFile, StandardCharsets.UTF_8)) {
                    Common.gson.toJson(data, writer);
                    Common.LOGGER.info("Saved coordinate '{}' for world '{}'", newCoord.getName(), worldName);
                }
            }
        } catch (IOException e) {
            Common.LOGGER.error("Failed to save coordinate to JSON", e);
        }
    }

    public static void deleteCoord(String worldName, String coordName) {
        try {
            Map<String, List<SavedCoord>> data = loadCoordsFromJson();
            if (data.containsKey(worldName)) {
                List<SavedCoord> worldCoords = data.get(worldName);
                worldCoords.removeIf(c -> c.getName().equalsIgnoreCase(coordName));

                File coordsFile = new File("Statify/savedCoords.json");
                if (createParentDirs(coordsFile)) {
                    try (FileWriter writer = new FileWriter(coordsFile, StandardCharsets.UTF_8)) {
                        Common.gson.toJson(data, writer);
                        Common.LOGGER.info("Deleted coordinate '{}' for world '{}'", coordName, worldName);
                    }
                }
            }
        } catch (IOException e) {
            Common.LOGGER.error("Failed to delete coordinate from JSON", e);
        }
    }

    public static List<SavedCoord> getCoordsForWorld(String worldName) {
        Map<String, List<SavedCoord>> data = loadCoordsFromJson();
        return data.getOrDefault(worldName, new ArrayList<>());
    }

    public static void writeStatsToFile(ServerPlayer player, String worldName) {
        CompletableFuture.runAsync(() -> {
            try {
                ServerStatsCounter statHandler = player.getStats();
                StringBuilder statsBuilder = new StringBuilder();

                for (Map.Entry<String, StatType<?>> categoryEntry : statCategories.entrySet()) {
                    String category = categoryEntry.getKey();
                    StatType<?> statType = categoryEntry.getValue();

                    for (Object key : statType.getRegistry()) {
                        Stat<?> stat = null;
                        String statName = null;
                        switch (key) {
                            case Identifier id -> {
                                stat = ((StatType<Identifier>) statType).get(id);
                                statName = id.toString();
                            }
                            case Block block -> {
                                stat = ((StatType<Block>) statType).get(block);
                                statName = BuiltInRegistries.BLOCK.getKey(block).toString();
                            }
                            case Item item -> {
                                stat = ((StatType<Item>) statType).get(item);
                                statName = BuiltInRegistries.ITEM.getKey(item).toString();
                            }
                            case EntityType<?> entity -> {
                                stat = ((StatType<EntityType<?>>) statType).get(entity);
                                statName = BuiltInRegistries.ENTITY_TYPE.getKey(entity).toString();
                            }
                            default -> {
                                Common.LOGGER.warn("Unexpected key type: {}", key.getClass().getName());
                            }
                        }

                        if (stat != null) {
                            int value = statHandler.getValue(stat);
                            statsBuilder.append(category).append(";").append(statName).append(";").append(value).append("\n");
                        }
                    }
                }

                Path statFilePath = Paths.get("Statify", worldName, "statify_stats.csv");
                File file = statFilePath.toFile();
                if (createParentDirs(file)) {
                    try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                        writer.write(statsBuilder.toString());
                    }
                }
            } catch (Exception e) {
                Common.LOGGER.error("Failed to write stats to CSV file", e);
            }
        });
    }

    public static void writeDaysToFile(String worldName, String days) {
        CompletableFuture.runAsync(() -> {
            try {
                Path daysFilePath = Paths.get("Statify", worldName, "days.csv");
                File dFile = daysFilePath.toFile();
                if (createParentDirs(dFile)) {
                    try (FileWriter writer = new FileWriter(dFile, StandardCharsets.UTF_8)) {
                        writer.write(days);
                    }
                }
            } catch (Exception e) {
                Common.LOGGER.error("Failed to write days to CSV file", e);
            }
        });
    }

    public static void savePlayerName(String playerName, String worldName) {
        try {
            String sanitizedWorldName = worldName.replaceAll("[:<>\"/\\\\|?*]", "_");

            Path path = Paths.get("Statify", sanitizedWorldName, "player.txt");
            File file = path.toFile();
            createParentDirs(file);

            try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                fw.write(playerName.replaceAll("literal\\{(.+?)}", "$1"));
            }
        } catch (IOException e) {
            Common.LOGGER.error("Failed to write player name to file", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadWorldStatus() {
        File configFile = new File("Statify/statify_worlds.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
                Type type = (new TypeToken<Map<String, String>>() {}).getType();
                Common.worldStatusMap = (Map<String, String>) Common.gson.fromJson(reader, type);
                Common.LOGGER.info("Loaded world status from JSON: {}", Common.worldStatusMap);
            } catch (IOException e) {
                Common.LOGGER.error("Failed to load world status from JSON", e);
            }
        }
    }

    public static void saveWorldStatus() {
        CompletableFuture.runAsync(() -> {
            try {
                File configFile = new File("Statify/statify_worlds.json");
                createParentDirs(configFile);

                try (FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8)) {
                    Common.gson.toJson(Common.worldStatusMap, writer);
                }

                Common.LOGGER.info("Saved world status to JSON: {}", Common.worldStatusMap);
            } catch (IOException e) {
                Common.LOGGER.error("Failed to save world status to JSON", e);
            }
        });
    }

    public static void saveSheetIdToJson(String worldName, String sheetId) {
        CompletableFuture.runAsync(() -> {
            try {
                Path path = Paths.get("Statify", worldName, "sheet_id.json");
                File file = path.toFile();
                createParentDirs(file);
                Map<String, String> sheetIdMap = new HashMap<>();
                sheetIdMap.put("sheetId", sheetId);

                try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
                    Common.gson.toJson(sheetIdMap, w);
                    Common.LOGGER.info("Saved Sheets ID: {} for world {}", sheetId, worldName);
                }
            } catch (IOException e) {
                Common.LOGGER.error("Failed to save Sheets ID for world: {}", worldName, e);
            }
        });
    }

    public static void removeSheetIdFromJson(String worldName, String sheetId) {
        CompletableFuture.runAsync(() -> {
            try {
                Path path = Paths.get("Statify", worldName, "sheet_id.json");
                File file = path.toFile();
                if (!file.exists()) {
                    Common.LOGGER.warn("Sheet ID file does not exist for world: {}", worldName);
                    return;
                }

                JsonObject json;
                try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                    json = Common.gson.fromJson(reader, JsonObject.class);
                } catch (IOException e) {
                    Common.LOGGER.error("Failed to read Sheet ID file for world: {}", worldName, e);
                    return;
                }

                if (json == null || !json.has("sheetId")) {
                    Common.LOGGER.warn("No sheetId found in file for world: {}", worldName);
                    return;
                }

                String currentSheetId = json.get("sheetId").getAsString();
                if (!currentSheetId.equals(sheetId)) {
                    Common.LOGGER.warn("The provided sheetId ({}) does not match the one in file ({}) for world: {}", sheetId, currentSheetId, worldName);
                    return;
                }

                json.remove("sheetId");
                Common.LOGGER.info("Removed sheetId: {} for world: {}", sheetId, worldName);

                try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                    Common.gson.toJson(json, writer);
                }
            } catch (Exception e) {
                Common.LOGGER.error("Failed to write updated sheetId file for world: {}", worldName, e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static String loadSheetIdFromJson(String worldName) {
        Path path = Paths.get("Statify", worldName, "sheet_id.json");
        File file = path.toFile();
        if (!file.exists()) {
            Common.LOGGER.warn("No Sheet ID found for world: {}", worldName);
            return null;
        } else {
            try (FileReader r = new FileReader(file, StandardCharsets.UTF_8)) {
                Type type = (new TypeToken<Map<String, String>>() {}).getType();
                Map<String, String> sheetIdMap = (Map<String, String>) Common.gson.fromJson(r, type);
                return sheetIdMap.getOrDefault("sheetId", null);
            } catch (IOException e) {
                Common.LOGGER.error("Failed to load Sheet's ID for world: {}", worldName, e);
                return null;
            }
        }
    }

    public static AchievementData loadAchievements(String worldName) {
        Path path = Paths.get("Statify", worldName, "achievements.json");
        File file = path.toFile();

        if (!file.exists()) {
            AchievementData newData = new AchievementData();
            saveAchievements(worldName, newData);
            return newData;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            AchievementData data = Common.gson.fromJson(reader, AchievementData.class);
            return data != null ? data : new AchievementData();
        } catch (IOException e) {
            Common.LOGGER.error("Failed to load achievements.json for world: {}", worldName);
            return new AchievementData();
        }
    }

    public static void saveAchievements(String worldName, AchievementData data) {
        CompletableFuture.runAsync(() -> {
            try {
                Path path = Paths.get("Statify", worldName, "achievements.json");
                File file = path.toFile();

                if (createParentDirs(file)) {
                    try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                        Common.gson.toJson(data, writer);
                    }
                }
            } catch (IOException e) {
                Common.LOGGER.error("Failed to save achievements.json for world: {}", worldName, e);
            }
        });
    }

    private static File getFolder(String worldName) {
        File folder = new File("saves/" + worldName + "/statify");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public static void saveCurrentSession(String worldName, String uuid, SessionData data) {
        File file = new File(getFolder(worldName), uuid + ".json");
        saveToFile(file, data);
    }

    public static SessionData loadCurrentSession(String worldName, String uuid) {
        File file = new File(getFolder(worldName), uuid + ".json");
        return loadFromFile(file);
    }

    public static void saveLastSnapshot(String worldName, String uuid, SessionData data) {
        File file = new File(getFolder(worldName), uuid + ".last.json");
        saveToFile(file, data);
    }

    public static SessionData loadLastSnapshot(String worldName, String uuid) {
        File file = new File(getFolder(worldName), uuid + ".last.json");
        return loadFromFile(file);
    }

    private static void saveToFile(File file, SessionData data) {
        try (FileWriter writer = new FileWriter(file)) {
            Common.gson.toJson(data, writer);
        } catch (IOException e) {
            Common.LOGGER.error("Hiba a session fájl mentésekor: {}", file.getName(), e);
        }
    }

    private static SessionData loadFromFile(File file) {
        if (!file.exists()) return null;
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, SessionData.class);
        } catch (IOException e) {
            Common.LOGGER.error("Hiba a session fájl beolvasásakor: {}", file.getName(), e);
            return null;
        }
    }

    static {
        statCategories.put("custom", Stats.CUSTOM);
        statCategories.put("mined", Stats.BLOCK_MINED);
        statCategories.put("used", Stats.ITEM_USED);
        statCategories.put("broken", Stats.ITEM_BROKEN);
        statCategories.put("crafted", Stats.ITEM_CRAFTED);
        statCategories.put("picked_up", Stats.ITEM_PICKED_UP);
        statCategories.put("dropped", Stats.ITEM_DROPPED);
        statCategories.put("killed", Stats.ENTITY_KILLED);
    }
}