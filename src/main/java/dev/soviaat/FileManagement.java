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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import static com.mojang.text2speech.Narrator.LOGGER;

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

    public static void writeStatsToFile(ServerPlayerEntity player, String worldName) {
        CompletableFuture.runAsync(() -> {
            try {
                StatHandler statHandler = player.getStatHandler();
                StringBuilder statsBuilder = new StringBuilder();

                for (Map.Entry<String, StatType<?>> categoryEntry : statCategories.entrySet()) {
                    String category = categoryEntry.getKey();
                    StatType<?> statType = categoryEntry.getValue();

                    for (Object entry : statType.getRegistry().getEntrySet()) {
                        if (entry instanceof Map.Entry<?, ?> typedEntry) {
                            Object key = typedEntry.getValue();
                            Stat<?> stat = null;
                            String statName = null;
                            switch (key) {
                                case Identifier id -> {
                                    stat = ((StatType<Identifier>) statType).getOrCreateStat(id);
                                    statName = id.toString();
                                }
                                case Block block -> {
                                    stat = ((StatType<Block>) statType).getOrCreateStat(block);
                                    statName = Registries.BLOCK.getId(block).toString();
                                }
                                case Item item -> {
                                    stat = ((StatType<Item>) statType).getOrCreateStat(item);
                                    statName = Registries.ITEM.getId(item).toString();
                                }
                                case EntityType entity -> {
                                    stat = ((StatType<EntityType>) statType).getOrCreateStat(entity);
                                    statName = Registries.ENTITY_TYPE.getId(entity).toString();
                                }
                                default -> {
                                    LOGGER.warn("Unexpected key type: {}", key.getClass().getName());
                                }
                            }

                            if (stat != null) {
                                int value = statHandler.getStat(stat);
                                statsBuilder.append(category).append(";").append(statName).append(";").append(value).append("\n");
                            }
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
                fw.write(playerName.replaceAll("literal\\{(.+?)\\}", "$1"));
            }
        } catch (IOException e) {
            Common.LOGGER.error("Failed to write player name to file", e);
        }
    }

    public static void loadWorldStatus() {
        File configFile = new File("Statify/statify_worlds.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
                Type type = (new TypeToken<Map<String, String>>() {}).getType();
                Common.worldStatusMap = (Map) Common.gson.fromJson(reader, type);
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

    public static String loadSheetIdFromJson(String worldName) {
        Path path = Paths.get("Statify", worldName, "sheet_id.json");
        File file = path.toFile();
        if (!file.exists()) {
            Common.LOGGER.warn("No Sheet ID found for world: {}", worldName);
            return null;
        } else {
            try (FileReader r = new FileReader(file, StandardCharsets.UTF_8)) {
                Type type = (new TypeToken<Map<String, String>>() {}).getType();
                Map<String, String> sheetIdMap = (Map) Common.gson.fromJson(r, type);
                return sheetIdMap.getOrDefault("sheetId", null);
            } catch (IOException e) {
                Common.LOGGER.error("Failed to load Sheet's ID for world: {}", worldName, e);
                return null;
            }
        }
    }

    static {
        statCategories.put("custom", Stats.CUSTOM);
        statCategories.put("mined", Stats.MINED);
        statCategories.put("used", Stats.USED);
        statCategories.put("broken", Stats.BROKEN);
        statCategories.put("crafted", Stats.CRAFTED);
        statCategories.put("picked_up", Stats.PICKED_UP);
        statCategories.put("dropped", Stats.DROPPED);
        statCategories.put("killed", Stats.KILLED);
    }
}
