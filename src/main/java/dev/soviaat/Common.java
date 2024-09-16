package dev.soviaat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Common {
    public static final String MOD_ID = "statify";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static Map<String, String> worldStatusMap = new HashMap<>();

    public static final String CONFIG_FILE = "Statify/statify_worlds.json";

    public static void putWorldStatus(String key, String value) {
        worldStatusMap.put(key, value);
    }
}
