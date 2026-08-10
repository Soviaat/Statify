package dev.soviaat.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.soviaat.Common;
import dev.soviaat.FileManagement;
import dev.soviaat.utils.UploadManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;

public class CommandManagement {
    private static UploadManager uploadManager;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(
                Commands.literal("statify")
                        .then(Commands.literal("help").executes(Help::HelpCmd))
                        .then(Commands.literal("sheetid").then(Commands.argument("sheetId", StringArgumentType.string()).executes(SheetId::sheetId)))
                        .then(Commands.literal("enable").executes(CommandManagement::statifyEnable))
                        .then(Commands.literal("disable").executes(CommandManagement::statifyDisable))
                        .then(
                                Commands.literal("upload")
                                        .then(Commands.literal("on").executes(CommandManagement::statifyUploadOn))
                                        .then(Commands.literal("off").executes(CommandManagement::statifyUploadOff))
                        )
        );
    }

    private static int statifyEnable(CommandContext<CommandSourceStack> ctx) {
        uploadManager = new UploadManager();
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();
        String currentStatus = Common.worldStatusMap.getOrDefault(worldName, "off");
        if ("on".equals(currentStatus)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Collection has already been §benabled§r for this world."), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Stat collection enabled for world: §l" + worldName), false);
            Common.putWorldStatus(worldName, "on");
            Common.putDayCount((int) (server.overworld().getLevelData().getGameTime() / 24000L));
            FileManagement.writeDaysToFile(worldName, Common.getDayCountAsString());
            FileManagement.saveWorldStatus();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                FileManagement.writeStatsToFile(player, worldName);
            }
        }

        return 1;
    }

    private static int statifyDisable(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();
        String currentStatus = Common.worldStatusMap.getOrDefault(worldName, "off");
        if ("off".equals(currentStatus)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Collection has already been §bdisabled§r for this world."), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Stat collection disabled for world: §l" + worldName), false);
            Common.putWorldStatus(worldName, "off");
            FileManagement.saveWorldStatus();
        }

        return 1;
    }

    private static int statifyUploadOn(CommandContext<CommandSourceStack> ctx) {
        MutableComponent noSheetIdFound = Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r No Google Sheet ID found for this world! Use ");
        MutableComponent clickable = Component.literal("§b§n/statify sheetid [ID]§r")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.SuggestCommand("/statify sheetid"))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to paste this command")))
                );
        noSheetIdFound.append(clickable);
        noSheetIdFound.append(" to set one.");

        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();
        String sheetId = FileManagement.loadSheetIdFromJson(worldName);
        if (sheetId == null) {
            ctx.getSource().sendSystemMessage(noSheetIdFound);
            return 1;
        } else {
            String currentUploadWorld = uploadManager.getUploadWorldAsync().join();
            if (currentUploadWorld != null && currentUploadWorld.equals(worldName)) {
                ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload is already §benabled§r for this world."), false);
            } else {
                if (currentUploadWorld != null) {
                    ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload has been disabled for §b" + currentUploadWorld + "§r."), false);
                }
                uploadManager.setUploadWorld(worldName);
                ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload has been §benabled§r for this world. (" + worldName + ")"), false);
            }

            MutableComponent restartMessage = Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r §cTo achieve proper functionality, §lplease restart the game.§r");
            ctx.getSource().sendSystemMessage(restartMessage);
            return 1;
        }
    }

    private static int statifyUploadOff(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getWorldData().getLevelName();
        if (uploadManager.isWorldUploading(worldName)) {
            uploadManager.clearUploadWorld();
            ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload has been §bdisabled§r for this world. (" + worldName + ")"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload is §balready disabled§r for this world."), false);
        }

        return 1;
    }
}