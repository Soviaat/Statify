package dev.soviaat.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.soviaat.FileManagement;
import dev.soviaat.SavedCoord;
import dev.soviaat.utils.CoordsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CoordCommands {

    public static int saveCoord(CommandContext<CommandSourceStack> ctx, String coordName, int x, int y, int z, String dimension) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();

        // Normalizáljuk a dimenzió nevet
        String dimClean = dimension.toLowerCase().replace("the_", "");
        if (!dimClean.equals("nether") && !dimClean.equals("end")) {
            dimClean = "overworld";
        }

        SavedCoord coord = new SavedCoord(coordName, x, y, z, dimClean, "minecraft:compass");
        FileManagement.saveCoord(worldName, coord);

        String finalDim = dimClean;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§o§7[" + StringUtils.capitalize("statify") + "]§r Saved coordinate §b" + coordName +
                        "§r (" + x + " " + y + " " + z + ") in §e" + finalDim.toUpperCase() + "§r for §l" + worldName
        ), false);

        return 1;
    }

    public static int saveCoordsHere(CommandContext<CommandSourceStack> ctx, String coordName) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();
        ServerPlayer player = server.getPlayerList().getPlayers().getFirst();

        int x = player.getBlockX();
        int y = player.getBlockY();
        int z = player.getBlockZ();

        String dimension = player.level().dimension().toString();

        String dimClean = dimension.toLowerCase().replace("the_", "");
        if (!dimClean.equals("nether") && !dimClean.equals("end")) {
            dimClean = "overworld";
        }

        SavedCoord coord = new SavedCoord(coordName, x, y, z, dimClean, "minecraft:compass");
        FileManagement.saveCoord(worldName, coord);

        String finalDim = dimClean;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§o§7[" + StringUtils.capitalize("statify") + "]§r Current position saved as §b" + coordName +
                        "§r (" + x + " " + y + " " + z + ") in §e" + finalDim.toUpperCase() + "§r for §l" + worldName
        ), false);

        return 1;
    }

    public static int getCoords(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();
        List<SavedCoord> coordsList = FileManagement.getCoordsForWorld(worldName);

        if (coordsList.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "§o§7[" + StringUtils.capitalize("statify") + "]§r No coordinates saved for §l" + worldName + "§r."
            ), false);
            return 1;
        }

        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreenAndShow(new CoordsScreen(worldName, coordsList));
        });

        return 1;
    }
}