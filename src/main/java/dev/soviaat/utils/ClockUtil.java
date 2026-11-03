package dev.soviaat.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;

public class ClockUtil {
    public static long getTotalTicks(ServerLevel level) {
        if (level == null) return 0L;

        return level.dimensionType().defaultClock()
                .map(clock -> level.clockManager().getInstance(clock))
                .map(ServerClockManager.ServerClockInstance::totalTicks)
                .orElseGet(() -> {
                    ServerLevel overworld = level.getServer().overworld();

                    return overworld.dimensionType().defaultClock()
                            .map(clock -> overworld.clockManager().getInstance(clock))
                            .map(ServerClockManager.ServerClockInstance::totalTicks).orElse(0L);
                });
    }

    public static long getDays(ServerLevel level) {
        return getTotalTicks(level) / 24000L;
    }
}
