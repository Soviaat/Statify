package dev.soviaat;

import dev.soviaat.dashboard.DashboardPrompt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

import static dev.soviaat.FileManagement.writeStatsToFile;

public class StatifyClient implements ClientModInitializer {
//    private boolean dashboardPromptShown = false;
//    private int tickDelay = 0;

    @Override
    public void onInitializeClient() {
//        ClientTickEvents.END_CLIENT_TICK.register(client -> {
//            if(client.world != null && !dashboardPromptShown) {
//                tickDelay++;
//
//                if(tickDelay > 10) {
//                    dashboardPromptShown = true;
//                    client.setScreen(new DashboardPrompt());
//                }
//            }
//
//            if(client.world != null && dashboardPromptShown && client.world.getServer() != null) {
//                String worldName = client.world.getServer().getSaveProperties().getLevelName();
//                ServerPlayerEntity player = client.world.getServer().getPlayerManager().getPlayerList().getFirst();
//                writeStatsToFile(player, worldName);
//            }
//        });
    }
}
