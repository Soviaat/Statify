package dev.soviaat.sessiontracker;

import java.util.HashMap;
import java.util.Map;

public class SessionData {
    public long timestamp = System.currentTimeMillis();
    public long days = 0;
    public long mobsKilled = 0;
    public Map<String, Double> blocksMined = new HashMap<>();
}
