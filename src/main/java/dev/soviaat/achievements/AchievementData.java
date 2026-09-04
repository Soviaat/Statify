package dev.soviaat.achievements;

import java.util.*;

public class AchievementData {
    public MilestoneGroup days = new MilestoneGroup(generateMilestones(List.of(10L, 50L, 100L, 250L, 500L, 1000L, 2500L, 5000L), 10000L, 100_000L, 10000L));
    public MilestoneGroup mobsKilled = new MilestoneGroup(generateMilestones(List.of(100L, 500L, 1000L, 5000L), 10000L, 1_000_000L, 10000L));
    public Map<String, MilestoneGroup> blocksMined = new HashMap<>();

    public static List<Long> generateMilestones(List<Long> base, long start, long end, long step) {
        List<Long> list = new ArrayList<>(base);
        for (long current = start; current <= end; current += step) {
            list.add(current);
        }
        return Collections.unmodifiableList(list);
    }

    public static List<Long> generateSmartBlockMilestones() {
        List<Long> list = new ArrayList<>(List.of(100L, 500L, 1000L, 5000L));

        for (long i = 10_000L; i < 100_000L; i += 10_000L) list.add(i); // 10,000 to 100,000 (steps up 10,000 each)

        for (long i = 100_000L; i < 1_000_000L; i += 100_000L) list.add(i); // then 100,000 to 1,000,000 (steps 100,000 each)

        for (long i = 1_000_000L; i <= 100_000_000L; i += 1_000_000L) list.add(i); // 1,000,000 to 100,000,000 (steps 1,000,000 each)

        return Collections.unmodifiableList(list);
    }
}

