package dev.soviaat.commands;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.soviaat.Common;
import dev.soviaat.FileManagement;
import dev.soviaat.utils.GoogleSheetsUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.List;

public class SheetId {
    public static int sheetId(CommandContext<CommandSourceStack> ctx) {
        String sheetId = StringArgumentType.getString(ctx, "sheetId");
        String worldName = ctx.getSource().getServer().getWorldData().getLevelName();
        FileManagement.saveSheetIdToJson(worldName, sheetId);
        boolean versionMatches = checkSheetVersion(worldName);
        Common.LOGGER.info("Version: {}", versionMatches);

        if (versionMatches) {
            ctx.getSource().sendSuccess(() -> Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Sheets ID set to: " + sheetId + " for " + worldName), false);
        } else {
            ctx.getSource().sendSuccess(SheetId::sendOutdatedSheetMessage, false);
            FileManagement.removeSheetIdFromJson(worldName, sheetId);
        }

        return 1;
    }

    public static MutableComponent sendOutdatedSheetMessage() {
        URI sheetURI = URI.create("https://docs.google.com/spreadsheets/d/1nGZAkqGMEmltLfvBtCr4GKUFrnvnlBlJlPddw4Wj6sc");
        String var10000 = StringUtils.capitalize("statify");
        MutableComponent baseMessage = Component.literal("§o§7[" + var10000 + "]§r Sheet version is outdated (Your version: " + Common.PLAYER_SHEET_VERSION + "), please update it to ");
        MutableComponent maskedUrl = Component.literal("§o§dlatest version (1.1)§r")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(sheetURI))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to update to the latest Sheet version"))));
        MutableComponent willNotWork = Component.literal(" §c(INFO: Sheet ID will not be initialized, please use the latest version.)§r");
        baseMessage.append(maskedUrl);
        baseMessage.append(willNotWork);
        return baseMessage;
    }

    public static boolean checkSheetVersion(String worldName) {
        try {
            Sheets sheetsService = GoogleSheetsUtil.getSheetService();
            String sheetId = FileManagement.loadSheetIdFromJson(worldName);
            String range = "Credits!H11";
            ValueRange res = sheetsService.spreadsheets().values().get(sheetId, range).execute();

            if (res.getValues() == null || res.getValues().isEmpty()) {
                Common.LOGGER.warn("Version not found in the specified Sheet.");
                return true;
            }

            String sheetVersion = (String)((List<?>) res.getValues().get(0)).get(0);

            try {
                float sheetVer = Float.parseFloat(sheetVersion);
                Common.PLAYER_SHEET_VERSION = String.valueOf(sheetVer);
                float currentVer = 1.1f;
                return sheetVer >= currentVer;
            } catch (NumberFormatException e) {
                Common.LOGGER.error("Invalid version format in sheet: {}", sheetVersion, e);
                return false;
            }
        } catch (GeneralSecurityException | IOException e) {
            Common.LOGGER.error("Failed to retrieve sheet version.", e);
            return true;
        }
    }
}