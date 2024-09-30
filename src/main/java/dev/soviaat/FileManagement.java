package dev.soviaat;

import com.google.common.reflect.TypeToken;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static dev.soviaat.Common.*;


public class FileManagement {
    static Map<String, StatType<?>> statCategories;
    static {
        statCategories = new LinkedHashMap<>();
        statCategories.put("custom", Stats.CUSTOM);
        statCategories.put("mined", Stats.MINED);
        statCategories.put("used", Stats.USED);
        statCategories.put("broken", Stats.BROKEN);
        statCategories.put("crafted", Stats.CRAFTED);
        statCategories.put("picked_up", Stats.PICKED_UP);
        statCategories.put("dropped", Stats.DROPPED);
        statCategories.put("killed", Stats.KILLED);
    }

    public static void writeStatsToFile(ServerPlayerEntity player, String worldName){
        CompletableFuture.runAsync(() -> {
            StatHandler statHandler = player.getStatHandler();
            StringBuilder statsBuilder = new StringBuilder();


            for (Map.Entry<String, StatType<?>> categoryEntry : statCategories.entrySet()) {
                String category = categoryEntry.getKey();
                StatType<?> statType = categoryEntry.getValue();

                for (Object entry : statType.getRegistry().getEntrySet()) {
                    if (entry instanceof Map.Entry<?, ?>) {
                        Map.Entry<?, ?> typedEntry = (Map.Entry<?, ?>) entry;
                        Object key = typedEntry.getValue();

                        Stat<?> stat = null;
                        String statName = null;

                        switch(key) {
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
            File parentDir = file.getParentFile();



            if(!parentDir.exists()) {
                boolean created = parentDir.mkdirs();

                if (created) {
                    LOGGER.info("Created directories: {}", parentDir.getAbsolutePath());
                } else {
                    LOGGER.error("Failed to create directories: {}", parentDir.getAbsolutePath());
                }
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(statsBuilder.toString());
            } catch (IOException e) {
                LOGGER.error("Failed to write stats to CSV file", e);
            }
        });
    }

    public static void writeDaysToFile(String worldName, String days) {
        CompletableFuture.runAsync(() -> {
            Path daysFilePath = Paths.get("Statify", worldName, "days.csv");
            File dFile = daysFilePath.toFile();
            File dParentDir = dFile.getParentFile();

            if(!dParentDir.exists()) {
                boolean days_file_created = dParentDir.mkdirs();
                if(days_file_created) {
                    LOGGER.info("Created days directories: {}", dParentDir.getAbsolutePath());
                } else {
                    LOGGER.error("Failed to create days directories: {}", dParentDir.getAbsolutePath());
                }
            }

            try (FileWriter writer = new FileWriter(dFile)) {
                writer.write(days);
            } catch(IOException e) {
                LOGGER.error("Failed to write days to CSV file", e);
            }
        });
    }

    public static void loadWorldStatus() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                worldStatusMap = gson.fromJson(reader, type);
                LOGGER.info("Loaded world status from JSON: {}", worldStatusMap);
            } catch (IOException e) {
                LOGGER.error("Failed to load world status from JSON", e);
            }
        }
    }

    public static void saveWorldStatus() {
        File configFile = new File(CONFIG_FILE);
        configFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(worldStatusMap, writer);
            LOGGER.info("Saved world status to JSON: {}", worldStatusMap);
        } catch (IOException e) {
            LOGGER.error("Failed to save world status to JSON", e);
        }
    }

    public static void saveSheetIdToJson(String worldName, String sheetId) {
        Path path = Paths.get("Statify", worldName, "sheet_id.json");
        File file = path.toFile();

        file.getParentFile().mkdirs();

        Map<String, String> sheetIdMap = new HashMap<>();
        sheetIdMap.put("sheetId", sheetId);

        try (FileWriter w = new FileWriter(file)) {
            gson.toJson(sheetIdMap, w);
            LOGGER.info("Saved Sheets ID: {} for world {}", sheetId, worldName);
        } catch (IOException e) {
            LOGGER.error("Failed to save Sheets ID to for world: {}", worldName, e);
        }
    }

    public static String loadSheetIdFromJson(String worldName) {
        Path path = Paths.get("Statify", worldName, "sheet_id.json");
        File file = path.toFile();

        if(!file.exists()) {
            LOGGER.warn("No Sheet ID found for world: {}", worldName);
            return null;
        }

        try (FileReader r = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> sheetIdMap = gson.fromJson(r, type);

            return sheetIdMap.getOrDefault("sheetId", null);
        } catch (IOException e) {
            LOGGER.error("Failed to load Sheets ID for world: {}", worldName, e);
            return null;
        }
    }
}
