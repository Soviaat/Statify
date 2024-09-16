package dev.soviaat;

import com.google.common.reflect.TypeToken;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static dev.soviaat.Common.*;


public class FileManagement {
    public static void writeStatsToFile(ServerPlayerEntity player, String worldName){
        StatHandler statHandler = player.getStatHandler();
        StringBuilder statsBuilder = new StringBuilder();

        Set<Map.Entry<RegistryKey<Identifier>, Identifier>> stats = Stats.CUSTOM.getRegistry().getEntrySet();

        for (Map.Entry<RegistryKey<Identifier>, Identifier> entry : stats) {
            Stat<Identifier> stat = Stats.CUSTOM.getOrCreateStat(entry.getValue());
            int value = statHandler.getStat(stat);
            String statName = entry.getValue().toString();

            statsBuilder.append(statName).append(";").append(value).append("\n");
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
}
