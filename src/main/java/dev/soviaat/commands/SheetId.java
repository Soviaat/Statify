package dev.soviaat.commands;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.soviaat.Common;
import dev.soviaat.utils.GoogleSheetsUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;

import static dev.soviaat.Common.*;
import static dev.soviaat.FileManagement.loadSheetIdFromJson;
import static dev.soviaat.FileManagement.removeSheetIdFromJson;
import static dev.soviaat.FileManagement.saveSheetIdToJson;

public class SheetId {
    public static int sheetId(CommandContext<ServerCommandSource> ctx) {
        String sheetId = StringArgumentType.getString(ctx, "sheetId");
        String worldName = ctx.getSource().getWorld().getServer().getSaveProperties().getLevelName();
        saveSheetIdToJson(worldName, sheetId);

        boolean versionMatches = checkSheetVersion(worldName);
        LOGGER.info("Version: {}", versionMatches);

        if(versionMatches) {
            ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Sheets ID set to: " + sheetId + " for " + worldName));
        } else {
            ctx.getSource().sendMessage(sendOutdatedSheetMessage());
            removeSheetIdFromJson(worldName, sheetId);
        }

        return 1;
    }

    public static MutableText sendOutdatedSheetMessage() {
        URI sheetURI = URI.create("https://docs.google.com/spreadsheets/d/1nGZAkqGMEmltLfvBtCr4GKUFrnvnlBlJlPddw4Wj6sc");
        MutableText baseMessage = Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Sheet version is outdated (Your version: "+ PLAYER_SHEET_VERSION + "), please update it to ");
        MutableText maskedUrl = Text.literal("§o§dlatest version ("+ CURRENT_SHEET_VERSION + ")§r")
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent.OpenUrl(sheetURI))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to update to the latest Sheet version")))
                );
        MutableText willNotWork = Text.literal(" §c(INFO: Sheet ID will not be initialized, please use the latest version.)§r");

        baseMessage.append(maskedUrl);
        baseMessage.append(willNotWork);
        return baseMessage;
    }

    public static boolean checkSheetVersion(String worldName) {
        /*
         *  Checks whether the sheet has the latest version.
         *  Returns false if the sheet version is older than the CURRENT_SHEET_VERSION.
         */
        try {
            Sheets sheetsService = GoogleSheetsUtil.getSheetService();
            String sheetId = loadSheetIdFromJson(worldName);
            String range = "Credits!H11";
            ValueRange res = sheetsService.spreadsheets().values().get(sheetId, range).execute();
            if (res.getValues() == null || res.getValues().isEmpty()) {
                Common.LOGGER.warn("Version not found in the specified Sheet.");
                return true; // If no version is found, skip version check
            }

            String sheetVersion = (String) res.getValues().get(0).get(0);

            // Numeric comparison
            try {
                float sheetVer = Float.parseFloat(sheetVersion);
                PLAYER_SHEET_VERSION = String.valueOf(sheetVer);
                float currentVer = Float.parseFloat(CURRENT_SHEET_VERSION);
                if (sheetVer < currentVer) {
                    return false;
                }
            } catch (NumberFormatException e) {
                Common.LOGGER.error("Invalid version format in sheet: {}", sheetVersion, e);
                return false;
            }
        } catch (IOException | GeneralSecurityException e) {
            Common.LOGGER.error("Failed to retrieve sheet version.", e);
        }
        return true;
    }
}
