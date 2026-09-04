package dev.soviaat.sessiontracker;

import dev.soviaat.FileManagement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    public static void onPlayerJoin(ServerPlayer player, String worldName, long currentDays) {
        String uuid = player.getStringUUID();

        SessionData lastSnapshot = FileManagement.loadLastSnapshot(worldName, uuid);

        SessionData currentState = captureCurrentState(player, currentDays);

        if (lastSnapshot != null) {
            long mobDelta = Math.max(0, currentState.mobsKilled - lastSnapshot.mobsKilled);
            long dayDelta = Math.max(0, currentState.days - lastSnapshot.days);

            Map<String, Long> blockDeltas = new HashMap<>();
            long totalBlocksMinedInSession = 0;

            for (Map.Entry<String, Double> entry : currentState.blocksMined.entrySet()) {
                String blockId = entry.getKey();
                long currentMined = entry.getValue().longValue();

                Double lastMinedDouble = lastSnapshot.blocksMined.get(blockId);
                long lastMined = (lastMinedDouble != null) ? lastMinedDouble.longValue() : 0L;

                long diff = currentMined - lastMined;
                if (diff > 0) {
                    String blockName = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(blockId))
                            .map(b -> b.getName().getString())
                            .orElse(blockId);

                    blockDeltas.put(blockName, diff);
                    totalBlocksMinedInSession += diff;
                }
            }

            long soundSeed = player.getRandom().nextLong();

            if (player.connection != null) {
                player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0f, 1.0f, soundSeed));
                player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0f, 1.0f, soundSeed));
                player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0f, 1.05f, soundSeed));
            }

            player.sendSystemMessage(Component.literal("§b★ §r§lLAST SESSION §r§b★"));
            player.sendSystemMessage(Component.literal("§7Days passed: §e" + ((dayDelta == 0) ? (dayDelta) : ("+" + dayDelta))));
            player.sendSystemMessage(Component.literal("§7Mobs killed: §e" + ((mobDelta == 0) ? (String.format("%,d", mobDelta)) : ("+" + String.format("%,d", mobDelta)))));
            player.sendSystemMessage(Component.literal("§7Total blocks mined: §e" + ((totalBlocksMinedInSession == 0) ? (String.format("%,d", totalBlocksMinedInSession)) : ("+" + String.format("%,d", totalBlocksMinedInSession)))));

            if (!blockDeltas.isEmpty()) {
                player.sendSystemMessage(Component.literal("§7Top 3 mined blocks:"));
                blockDeltas.entrySet().stream()
                        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                        .limit(3)
                        .forEach(entry -> player.sendSystemMessage(
                                Component.literal("  §8• §f" + entry.getKey() + ": §e+" + String.format("%,d", entry.getValue()))
                        ));
            }

        }

        FileManagement.saveLastSnapshot(worldName, uuid, currentState);

        FileManagement.saveCurrentSession(worldName, uuid, currentState);
    }

    public static void updateCurrentSession(ServerPlayer player, String worldName, long currentDays) {
        SessionData currentState = captureCurrentState(player, currentDays);
        FileManagement.saveCurrentSession(worldName, player.getStringUUID(), currentState);
    }

    public static SessionData captureCurrentState(ServerPlayer player, long currentDays) {
        SessionData data = new SessionData();
        data.days = currentDays;
        data.mobsKilled = player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));

        for (Block block : BuiltInRegistries.BLOCK) {
            int mined = player.getStats().getValue(Stats.BLOCK_MINED.get(block));
            if (mined > 0) {
                String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
                data.blocksMined.put(blockId, (double) mined);
            }
        }
        return data;
    }
}