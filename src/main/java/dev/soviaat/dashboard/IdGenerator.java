package dev.soviaat.dashboard;

import java.util.UUID;

import static dev.soviaat.Common.LOGGER;

public class IdGenerator {
    private static String lastId;

    public static String generateId() {
        lastId = UUID.randomUUID().toString();
        return lastId;
    }

    public static String getLastGeneratedId() {
        return lastId;
    }
}
