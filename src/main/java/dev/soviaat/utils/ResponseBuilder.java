package dev.soviaat.utils;

import java.net.URI;
import net.minecraft.text.ClickEvent; // class_2558;
import net.minecraft.text.Text; // 2561
import net.minecraft.text.HoverEvent; // 2568
import net.minecraft.text.Style; // 2583
import net.minecraft.text.MutableText; // 5250
import org.apache.commons.lang3.StringUtils;

public class ResponseBuilder {
    public static MutableText Builder(String response, boolean isError) {
        MutableText var2;
        if (isError) {
            String var10000 = StringUtils.capitalize("statify");
            var2 = Text.literal("§o§7[" + var10000 + "]§r " + response);
        } else {
            String var3 = StringUtils.capitalize("statify");
            var2 = Text.literal("§o§7[" + var3 + "]§r §c" + response + "§r");
        }

        return var2;
    }

    public static MutableText Builder(String response, boolean isError, String hoverText, String hoverDescription) {
        MutableText var6;
        if (isError) {
            String var10000 = StringUtils.capitalize("statify");
            var6 = Text.literal("§o§7[" + var10000 + "]§r " + response);
        } else {
            String var7 = StringUtils.capitalize("statify");
            var6 = Text.literal("§o§7[" + var7 + "]§r §c" + response + "§r");
        }

        MutableText baseResponse = var6;
        MutableText maskedUrl = Text.literal("§o§d" + hoverText + "§r").setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverDescription))));
        baseResponse.append(maskedUrl);
        return baseResponse;
    }

    public static MutableText Builder(String response, boolean isError, String hoverText, String hoverDescription, String clickText, ClickEvent.Action clickAction) {
        MutableText var12;
        if (!isError) {
            String var10000 = StringUtils.capitalize("statify");
            var12 = Text.literal("§o§7[" + var10000 + "]§r " + response);
        } else {
            String var13 = StringUtils.capitalize("statify");
            var12 = Text.literal("§o§7[" + var13 + "]§r §c" + response + "§r");
        }

        MutableText baseResponse = var12;
        switch (clickAction) {
            case OPEN_URL:
                URI textUri = URI.create(clickText);
                ClickEvent click = new ClickEvent.OpenUrl(textUri);
                MutableText maskedUrl = Text.literal("§o§d" + hoverText + "§r").setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverDescription))).withClickEvent(click));
                baseResponse.append(maskedUrl);
                return baseResponse;
            case SUGGEST_COMMAND:
                ClickEvent suggest = new ClickEvent.SuggestCommand(clickText);
                MutableText maskedSuggestion = Text.literal("§o§d" + hoverText + "§r").setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverDescription))).withClickEvent(suggest));
                baseResponse.append(maskedSuggestion);
                return baseResponse;
            default:
                return baseResponse;
        }
    }
}