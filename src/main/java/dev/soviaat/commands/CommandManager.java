package dev.soviaat.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.soviaat.Common;
import dev.soviaat.FileManagement;
import dev.soviaat.utils.UploadManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

public class CommandManagement {
    private static UploadManager uploadManager;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("statify")
                        .then(CommandManager.literal("help").executes(Help::HelpCmd))
                        .then(CommandManager.literal("sheetid").then(CommandManager.argument("sheetId", StringArgumentType.string()).executes(SheetId::sheetId)))
                        .then(CommandManager.literal("enable").executes(CommandManagement::statifyEnable))
                        .then(CommandManager.literal("disable").executes(CommandManagement::statifyDisable))
                        .then(
                                CommandManager.literal("upload")
                                        .then(CommandManager.literal("on").executes(CommandManagement::statifyUploadOn))
                                        .then(CommandManager.literal("off").executes(CommandManagement::statifyUploadOff))
                        )
        );
    }

    private static int statifyEnable(CommandContext<ServerCommandSource> ctx) {
        uploadManager = new UploadManager();
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getSaveProperties().getLevelName();
        String currentStatus = Common.worldStatusMap.getOrDefault(worldName, "off");
        if ("on".equals(currentStatus)) {
            ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Collection has already been §benabled§r for this world."), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Stat collection enabled for world: §l" + worldName), false);
            Common.putWorldStatus(worldName, "on");
            Common.putDayCount((int) (server.getOverworld().getTimeOfDay() / 24000L));
            FileManagement.writeDaysToFile(worldName, Common.getDayCountAsString());
            FileManagement.saveWorldStatus();

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                FileManagement.writeStatsToFile(player, worldName);
            }
        }

        return 1;
    }

    private static int statifyDisable(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getSaveProperties().getLevelName();
        String currentStatus = Common.worldStatusMap.getOrDefault(worldName, "off");
        if ("off".equals(currentStatus)) {
            ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Collection has already been §bdisabled§r for this world."), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Stat collection disabled for world: §l" + worldName), false);
            Common.putWorldStatus(worldName, "off");
            FileManagement.saveWorldStatus();
        }

        return 1;
    }

    private static int statifyUploadOn(CommandContext<ServerCommandSource> ctx) {
        MutableText noSheetIdFound = Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r No Google Sheet ID found for this world! Use ");
        MutableText clickable = Text.literal("§b§n/statify sheetid [ID]§r")
                .styled(style -> style.withClickEvent(new ClickEvent.SuggestCommand("/statify sheetid"))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to paste this command"))));
        noSheetIdFound.append(clickable);
        noSheetIdFound.append(" to set one.");

        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getSaveProperties().getLevelName();
        String sheetId = FileManagement.loadSheetIdFromJson(worldName);
        if (sheetId == null) {
            ctx.getSource().sendMessage(noSheetIdFound);
            return 1;
        } else {
            String currentUploadWorld = uploadManager.getUploadWorldAsync().join();
            if (currentUploadWorld != null && currentUploadWorld.equals(worldName)) {
                ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload is already §benabled§r for this world."), false);
            } else {
                if (currentUploadWorld != null) {
                    ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload has been disabled for §b" + currentUploadWorld + "§r."), false);
                }
                uploadManager.setUploadWorld(worldName);
                ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload has been §benabled§r for this world. (" + worldName + ")"), false);
            }

            MutableText restartMessage = Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r §cTo achieve proper functionality, §lplease restart the game.§r");
            ctx.getSource().sendMessage(restartMessage);
            return 1;
        }
    }

    private static int statifyUploadOff(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        String worldName = server.getSaveProperties().getLevelName();
        if (uploadManager.isWorldUploading(worldName)) {
            uploadManager.clearUploadWorld();
            ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload has been §bdisabled§r for this world. (" + worldName + ")"), false);
        } else {
            ctx.getSource().sendFeedback(() -> Text.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Upload is §balready disabled§r for this world."), false);
        }

        return 1;
    }
}
