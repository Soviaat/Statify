package dev.soviaat.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.soviaat.Common;
import dev.soviaat.FileManagement;
import dev.soviaat.utils.CoordsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoordCommands {
    public static int saveCoord(CommandContext<CommandSourceStack> ctx, String coordName, int x, int y, int z) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();
        String coordString = x + " " + y + " " + z;

        FileManagement.saveCoord(worldName, coordName, coordString);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§o§7[" + StringUtils.capitalize("statify") + "]§r Saved coordinate §b" + coordName + "§r as §a" + coordString + "§r in §l" + worldName
        ), false);

        return 1;

    }

    @SuppressWarnings("SameReturnValue")
    public static int getCoords(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();
        Map<String, String> coordsMap = FileManagement.getCoordsForWorld(worldName);

        if (coordsMap.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "§o§7[" + StringUtils.capitalize("statify") + "]§r No coordinates saved for §l" + worldName + "§r."
            ), false);
            return 1;
        }

//        ctx.getSource().sendSuccess(() -> Component.literal("§l§6SAVED COORDINATES§r\n"), false);
//
//        int index = 1;
//        for (Map.Entry<String, String> entry : coordsMap.entrySet()) {
//            final int currentIndex = index;
//            String name = entry.getKey();
//            String pos = entry.getValue();
//
//            ctx.getSource().sendSuccess(() -> Component.literal(
//                    "§7[" + currentIndex + "] §b" + name + "§r - §a" + pos
//            ), false);
//            index++;
//        }

        List<String> formattedList = new ArrayList<>();
        for (Map.Entry<String, String> entry : coordsMap.entrySet()) {
            formattedList.add(entry.getKey() + " - " + entry.getValue());
        }

        Common.LOGGER.info(String.valueOf(formattedList));

        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreenAndShow(new CoordsScreen(worldName, formattedList));
        });

        return 1;
    }

}
