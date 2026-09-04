package dev.soviaat.achievements;

import java.util.ArrayList;
import java.util.List;

public class MilestoneGroup {
    public long current = 0;
    public long lastUnlocked = 0;
    public List<Long> milestones = new ArrayList<>();

    public MilestoneGroup() {}

    public MilestoneGroup(List<Long> defaultMilestones) {
        this.milestones = defaultMilestones;
    }
}
