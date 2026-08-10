package dev.soviaat.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class Help {
    public static final Map<String, String> commands = new LinkedHashMap<>();

    public static int HelpCmd(CommandContext<CommandSourceStack> ctx) {
        URI url = URI.create("https://github.com/Soviaat/Statify#setup");
        MutableComponent baseHelpMessage = Component.literal("§o§7[" + StringUtils.capitalize("statify") + "]§r Tutorial on how to set up your Statistics Sheets ");
        MutableComponent maskedUrl = Component.literal("§o§d[CLICK HERE]§r")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(url))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to open tutorial (" + url + ")"))));
        baseHelpMessage.append(maskedUrl);

        for (Map.Entry<String, String> entry : commands.entrySet()) {
            String cmd = entry.getKey();
            String desc = entry.getValue();
            String cleanedCommand = cleanCommand(cmd);
            MutableComponent clickableCmd = Component.literal(cmd)
                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand(cleanedCommand))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to paste this command: " + cleanedCommand))));
            MutableComponent cmdWithDesc = Component.literal("").append(clickableCmd).append(Component.literal(" - " + desc));
            ctx.getSource().sendSuccess(() -> cmdWithDesc, false);
        }

        ctx.getSource().sendSuccess(() -> baseHelpMessage, false);
        return 1;
    }

    private static String cleanCommand(String command) {
        return command.replaceAll("§7\\[.*?]§r", "")
                .replaceAll("§7<.*?>§r", "")
                .replace("§b", "")
                .replace("§r", "")
                .trim();
    }

    static {
        commands.put("§b/statify help§r", "Displays this help message.");
        commands.put("§b/statify disable§r", "Disables stat collection.");
        commands.put("§b/statify enable§r", "Enables stat collection.");
        commands.put("§b/statify sheetid §7[string: id]§r", "Lets you specify the Google Sheets ID.");
        commands.put("§b/statify upload §7<on|off>§r", "Lets you toggle the uploading to Google Sheets.");
    }
}