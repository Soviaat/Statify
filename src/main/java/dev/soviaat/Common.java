package dev.soviaat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Common {
    public static final String MOD_ID = "statify";
    public static final Logger LOGGER = LoggerFactory.getLogger("statify");
    public static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
    public static Map<String, String> worldStatusMap = new HashMap();
    public static int dayCount;
    public static final String CURRENT_SHEET_VERSION = "1.1";
    public static String PLAYER_SHEET_VERSION;
    public static final String CONFIG_FILE = "Statify/statify_worlds.json";

    public static void putWorldStatus(String key, String value) {
        worldStatusMap.put(key, value);
    }

    public static void putDayCount(int dayCount) {
        CompletableFuture.runAsync(() -> Common.dayCount = dayCount);
    }

    public static int getDayCount(String world) {
        int days = 0;
        Path path = Paths.get("Statify", world, "days.csv");
        File file = path.toFile();
        File parentD = file.getParentFile();
        if (parentD != null) {
            try {
                days = Integer.parseInt(Files.readString(path));
            } catch (IOException e) {
                LOGGER.error("Failed to read days from CSV file", e);
            }
        }

        return days;
    }

    public static String getDayCountAsString() {
        return "" + dayCount;
    }
}
