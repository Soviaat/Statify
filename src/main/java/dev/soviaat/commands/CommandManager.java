package dev.soviaat.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.soviaat.FileManagement;
import dev.soviaat.utils.UploadManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.apache.commons.lang3.StringUtils;

import static dev.soviaat.Common.*;
import static dev.soviaat.FileManagement.*;

public class CommandManager {
    private static UploadManager uploadManager;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, net.minecraft.server.command.CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(net.minecraft.server.command.CommandManager.literal("statify")
                .then(net.minecraft.server.command.CommandManager.literal("help")
                        .executes(Help::HelpCmd)
                )
                .then(net.minecraft.server.command.CommandManager.literal("sheetid")
                        .then(net.minecraft.server.command.CommandManager.argument("sheetId", StringArgumentType.string())
                                .executes(SheetId::sheetId))
                )
                .then(net.minecraft.server.command.CommandManager.literal("enable")
                        .executes(CommandManager::statifyEnable)
                )
                .then(net.minecraft.server.command.CommandManager.literal("disable")
                        .executes(CommandManager::statifyDisable)
                )
                .then(net.minecraft.server.command.CommandManager.literal("upload")
                        .then(net.minecraft.server.command.CommandManager.literal("on")
                                .executes(CommandManager::statifyUploadOn)
                        )
                        .then(net.minecraft.server.command.CommandManager.literal("off")
                                .executes(CommandManager::statifyUploadOff)
                )
            )
        );
    }

    private static int statifyEnable(CommandContext<ServerCommandSource> ctx) {
        uploadManager = new UploadManager();
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getOverworld().getServer().getSaveProperties().getLevelName();

        String currentStatus = worldStatusMap.getOrDefault(worldName, "off");

        if ("on".equals(currentStatus)) {
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID)  + "]§r Collection has already been §benabled§r for this world."));
        } else {
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID)  + "]§r Stat collection enabled for world: §l" + worldName));
            putWorldStatus(worldName, "on");
            putDayCount((int) (server.getOverworld().getTimeOfDay() / 24000L));
            writeDaysToFile(worldName, getDayCountAsString());
            saveWorldStatus();

            for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                writeStatsToFile(player, worldName);
            }
        }
        return 1;
    }

    private static int statifyDisable(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getOverworld().getServer().getSaveProperties().getLevelName();

        String currentStatus = worldStatusMap.getOrDefault(worldName, "off");
        if("off".equals(currentStatus)) {
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Collection has already been §bdisabled§r for this world."));
        } else {
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Stat collection disabled for world: §l" + worldName));
            putWorldStatus(worldName, "off");
            saveWorldStatus();
        }

        return 1;
    }

    private static int statifyUploadOn(CommandContext<ServerCommandSource> ctx) {
        MutableText noSheetIdFound = Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r No Google Sheet ID found for this world! Use ");
        MutableText clickable = Text.literal("§b§n/statify sheetid [ID]§r")
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/statify sheetid "))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to paste this command."))));

        noSheetIdFound.append(clickable);
        noSheetIdFound.append(" to set one.");

        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getOverworld().getServer().getSaveProperties().getLevelName();

        String sheetId = FileManagement.loadSheetIdFromJson(worldName);
        if(sheetId == null) {
            ctx.getSource().sendFeedback(() -> Text.of(noSheetIdFound), false);
            return 1;
        }

        String currentUploadWorld = uploadManager.getUploadWorld();

        if (currentUploadWorld != null && currentUploadWorld.equals(worldName)) {
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload is already §benabled§r for this world."));
        } else {
            if (currentUploadWorld != null) {
                ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload has been disabled for §b" + currentUploadWorld + "§r."));
            }

            uploadManager.setUploadWorld(worldName);
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload has been §benabled§r for this world. (" + worldName + ")"));
        }

        return 1;
    }

    private static int statifyUploadOff(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getOverworld().getServer().getSaveProperties().getLevelName();

        if(uploadManager.isWorldUploading(worldName)) {
            uploadManager.clearUploadWorld();
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload has been §bdisabled§r for this world. (" + worldName + ")"));
        } else {
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Upload is §balready disabled§r for this world."));
        }

        return 1;
    }
}
