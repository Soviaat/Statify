package dev.soviaat.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;

public class Help {

    public static int HelpCmd(CommandContext<CommandSourceStack> ctx) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreenAndShow(new HelpScreen());
        });
        return 1;
    }
}