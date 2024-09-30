package dev.soviaat.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import static dev.soviaat.Common.MOD_ID;
import static dev.soviaat.FileManagement.saveSheetIdToJson;

public class SheetId {
    public static int sheetId(CommandContext<ServerCommandSource> ctx) {
        String sheetId = StringArgumentType.getString(ctx, "sheetId");
        String worldName = ctx.getSource().getWorld().getServer().getSaveProperties().getLevelName();

        saveSheetIdToJson(worldName, sheetId);

        ctx.getSource().sendMessage(Text.of("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Sheets ID set to: " + sheetId + " for " + worldName));
        return 1;
    }
}
