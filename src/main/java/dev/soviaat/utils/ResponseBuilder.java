package dev.soviaat.utils;

import com.mojang.datafixers.kinds.IdF;
import net.minecraft.text.*;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

import static dev.soviaat.Common.MOD_ID;

public class ResponseBuilder {

    public static MutableText Builder(String response, boolean isError) {
        return isError ? (Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r " + response)) :
                    (Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r §c" + response + "§r"));

    }

    public static MutableText Builder(String response, boolean isError, String hoverText, String hoverDescription) {
        MutableText baseResponse = isError ? (Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r " + response)) :
                (Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r §c" + response + "§r"));

        MutableText maskedUrl = Text.literal("§o§d" + hoverText + "§r")
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverDescription))));
        baseResponse.append(maskedUrl);
        return baseResponse;
    }

    public static MutableText Builder(String response, boolean isError, String hoverText, String hoverDescription, String clickText, ClickEvent.Action clickAction) {
        MutableText baseResponse = !isError ? (Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r " + response)) :
                (Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r §c" + response + "§r"));

        switch (clickAction) {
            case OPEN_URL:
                URI textUri = URI.create(clickText);
                ClickEvent click = new ClickEvent.OpenUrl(textUri);
                MutableText maskedUrl = Text.literal("§o§d" + hoverText + "§r")
                        .setStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverDescription)))
                                .withClickEvent(click));
                baseResponse.append(maskedUrl);
                return baseResponse;
            case SUGGEST_COMMAND:
                ClickEvent suggest = new ClickEvent.SuggestCommand(clickText);
                MutableText maskedSuggestion = Text.literal("§o§d" + hoverText + "§r")
                        .setStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverDescription)))
                                .withClickEvent(suggest));
                baseResponse.append(maskedSuggestion);
                return baseResponse;
        }

        return baseResponse;
    }
}

/*
     MutableText baseOutdated =  Text.literal("§o§7[" + StringUtils.capitalize(MOD_ID) + "]§r Sheet version is outdated, please update it to ");
    MutableText maskedUrl = Text.literal("§o§d[CLICK HERE]§r")
            .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.google.com/spreadsheets/d/1nGZAkqGMEmltLfvBtCr4GKUFrnvnlBlJlPddw4Wj6sc"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to update the latest Sheet version"))));

            baseOutdated.append(maskedUrl);
*/