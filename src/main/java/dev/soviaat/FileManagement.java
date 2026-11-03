package dev.soviaat;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

public class FileManagement {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static Map<String, StatType<?>> statCategories = new LinkedHashMap<>();

    private static boolean createParentDirs(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            return parentDir.mkdirs();
        }
        return true;
    }

    public static <T> T loadJson(File file, Type typeOfT, T fallback) {
        if (!file.exists()) return fallback;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            T result = GSON.fromJson(reader, typeOfT);
            return result != null ? result : fallback;
        } catch (Exception e) {
            Common.LOGGER.error("Hiba a JSON beolvasásakor: {}", file.getPath(), e);
            return fallback;
        }
    }

    public static void saveJson(File file, Object data) {
        if (!createParentDirs(file)) return;
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        } catch (Exception e) {
            Common.LOGGER.error("Hiba a JSON mentésekor: {}", file.getPath(), e);
        }
    }

    public static void saveJsonAsync(File file, Object data) {
        CompletableFuture.runAsync(() -> saveJson(file, data));
    }

    private static final File COORDS_FILE = new File("Statify/savedCoords.json");
    private static final Type COORDS_TYPE = new TypeToken<Map<String, List<SavedCoord>>>() {}.getType();

    public static Map<String, List<SavedCoord>> loadCoordsFromJson() {
        return loadJson(COORDS_FILE, COORDS_TYPE, new LinkedHashMap<>());
    }

    public static void saveCoord(String worldName, SavedCoord newCoord) {
        Map<String, List<SavedCoord>> data = loadCoordsFromJson();
        List<SavedCoord> worldCoords = data.computeIfAbsent(worldName, k -> new ArrayList<>());
        worldCoords.removeIf(c -> c.getName().equalsIgnoreCase(newCoord.getName()));
        worldCoords.add(newCoord);
        saveJson(COORDS_FILE, data);
    }

    public static void deleteCoord(String worldName, String coordName) {
        Map<String, List<SavedCoord>> data = loadCoordsFromJson();
        if (data.containsKey(worldName)) {
            data.get(worldName).removeIf(c -> c.getName().equalsIgnoreCase(coordName));
            saveJson(COORDS_FILE, data);
        }
    }

    public static List<SavedCoord> getCoordsForWorld(String worldName) {
        return loadCoordsFromJson().getOrDefault(worldName, new ArrayList<>());
    }

    public static AchievementData loadAchievements(String worldName) {
        File file = Paths.get("Statify", worldName, "achievements.json").toFile();
        return loadJson(file, AchievementData.class, new AchievementData());
    }

    public static void saveAchievements(String worldName, AchievementData data) {
        File file = Paths.get("Statify", worldName, "achievements.json").toFile();
        saveJsonAsync(file, data);
    }

    private static File getSessionFolder(String worldName) {
        File folder = new File("saves/" + worldName + "/statify");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public static void saveCurrentSession(String worldName, String uuid, SessionData data) {
        saveJson(new File(getSessionFolder(worldName), uuid + ".json"), data);
    }

    public static SessionData loadCurrentSession(String worldName, String uuid) {
        return loadJson(new File(getSessionFolder(worldName), uuid + ".json"), SessionData.class, null);
    }

    public static void saveLastSnapshot(String worldName, String uuid, SessionData data) {
        saveJson(new File(getSessionFolder(worldName), uuid + ".last.json"), data);
    }

    public static SessionData loadLastSnapshot(String worldName, String uuid) {
        return loadJson(new File(getSessionFolder(worldName), uuid + ".last.json"), SessionData.class, null);
    }

    private static final File WORLD_STATUS_FILE = new File("Statify/statify_worlds.json");
    private static final Type MAP_STRING_STRING = new TypeToken<Map<String, String>>() {}.getType();

    public static void loadWorldStatus() {
        Common.worldStatusMap = loadJson(WORLD_STATUS_FILE, MAP_STRING_STRING, new HashMap<>());
    }

    public static void saveWorldStatus() {
        saveJsonAsync(WORLD_STATUS_FILE, Common.worldStatusMap);
    }

    public static void saveSheetIdToJson(String worldName, String sheetId) {
        File file = Paths.get("Statify", worldName, "sheet_id.json").toFile();
        Map<String, String> map = new HashMap<>();
        map.put("sheetId", sheetId);
        saveJsonAsync(file, map);
    }

    public static String loadSheetIdFromJson(String worldName) {
        File file = Paths.get("Statify", worldName, "sheet_id.json").toFile();
        Map<String, String> map = loadJson(file, MAP_STRING_STRING, null);
        return map != null ? map.get("sheetId") : null;
    }

    public static void removeSheetIdFromJson(String worldName, String sheetId) {
        CompletableFuture.runAsync(() -> {
            File file = Paths.get("Statify", worldName, "sheet_id.json").toFile();
            JsonObject json = loadJson(file, JsonObject.class, null);
            if (json != null && json.has("sheetId") && json.get("sheetId").getAsString().equals(sheetId)) {
                json.remove("sheetId");
                saveJson(file, json);
            }
        });
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

                        if (key instanceof Identifier id) {
                            stat = ((StatType<Identifier>) statType).get(id);
                            statName = id.toString();
                        } else if (key instanceof Block block) {
                            stat = ((StatType<Block>) statType).get(block);
                            statName = BuiltInRegistries.BLOCK.getKey(block).toString();
                        } else if (key instanceof Item item) {
                            stat = ((StatType<Item>) statType).get(item);
                            statName = BuiltInRegistries.ITEM.getKey(item).toString();
                        } else if (key instanceof EntityType<?> entity) {
                            stat = ((StatType<EntityType<?>>) statType).get(entity);
                            statName = BuiltInRegistries.ENTITY_TYPE.getKey(entity).toString();
                        }

                        if (stat != null) {
                            int value = statHandler.getValue(stat);
                            statsBuilder.append(category).append(";").append(statName).append(";").append(value).append("\n");
                        }
                    }
                }

                File file = Paths.get("Statify", worldName, "statify_stats.csv").toFile();
                if (createParentDirs(file)) {
                    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
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
            File dFile = Paths.get("Statify", worldName, "days.csv").toFile();
            if (createParentDirs(dFile)) {
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dFile), StandardCharsets.UTF_8)) {
                    writer.write(days);
                } catch (IOException e) {
                    Common.LOGGER.error("Failed to write days to CSV", e);
                }
            }
        });
    }

    public static void savePlayerName(String playerName, String worldName) {
        String sanitizedWorldName = worldName.replaceAll("[:<>\"/\\\\|?*]", "_");
        File file = Paths.get("Statify", sanitizedWorldName, "player.txt").toFile();
        if (createParentDirs(file)) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(playerName.replaceAll("literal\\{(.+?)}", "$1"));
            } catch (IOException e) {
                Common.LOGGER.error("Failed to write player name", e);
            }
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