package dev.soviaat.achievements;

import dev.soviaat.FileManagement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dev.soviaat.achievements.AchievementData.generateSmartBlockMilestones;

public class MilestoneManager {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final List<Long> DEFAULT_BLOCK_MILESTONES = generateSmartBlockMilestones();

    public static void checkDaysOnly(ServerPlayer player, String worldName, long currentDays) {
        AchievementData data = FileManagement.loadAchievements(worldName);

        if (data.days.current != currentDays) {
            data.days.current = currentDays;
            if (checkGroup(data.days, player, "You've reached %d days!")) {
                FileManagement.saveAchievements(worldName, data);
            }
        }
    }

    public static void checkAndTriggerMilestones(ServerPlayer player, String worldName) {
        AchievementData data = FileManagement.loadAchievements(worldName);
        boolean changed = false;

        long mobsKilled = player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        data.mobsKilled.current = mobsKilled;
        if (checkGroup(data.mobsKilled, player, "You've killed %,d mobs!")) {
            changed = true;
        }

        for (Block block : BuiltInRegistries.BLOCK) {
            int mined = player.getStats().getValue(Stats.BLOCK_MINED.get(block));

            if (mined > 0) {
                String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
                MilestoneGroup group = data.blocksMined.computeIfAbsent(
                        blockId,
                        k -> new MilestoneGroup(DEFAULT_BLOCK_MILESTONES)
                );

                group.current = mined;
                String blockName = block.getName().getString();
                if (checkGroup(group, player, "You've mined %,d " + blockName + "!")) {
                    changed = true;
                }
            }
        }

        if (changed) {
            FileManagement.saveAchievements(worldName, data);
        }
    }

    private static boolean checkGroup(MilestoneGroup group, ServerPlayer player, String messageFormat) {
        boolean updated = false;

        for (long milestone : group.milestones) {
            if (group.current >= milestone && group.lastUnlocked < milestone) {
                group.lastUnlocked = milestone;
                updated = true;

                String formatted = String.format(messageFormat, milestone);

                Component message = Component.literal("§6★ §r§lMilestone Reached: §r§o§e" + formatted);
                sendActionBarForDuration(player, message, 10);
                player.level().playSound(
                        null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f
                );
            }
        }

        return updated;
    }

    private static void sendActionBarForDuration(ServerPlayer player, Component message, int durationInSeconds) {
        for (int i = 0; i < durationInSeconds; i++) {
            scheduler.schedule(() -> {
                if (player != null && player.connection != null && !player.hasDisconnected()) {
                    player.sendSystemMessage(message, true);
                }
            }, i, TimeUnit.SECONDS);
        }
    }
}