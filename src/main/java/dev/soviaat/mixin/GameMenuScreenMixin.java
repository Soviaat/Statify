package dev.soviaat.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.soviaat.Common;
import dev.soviaat.utils.StatifySettingsScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PauseScreen.class)
public class GameMenuScreenMixin extends Screen {

    private static final WidgetSprites VANILLA_BUTTON_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath("minecraft", "widget/button"),
            Identifier.fromNamespaceAndPath("minecraft", "widget/button_highlighted")
    );

    private static final Identifier STATIFY_ICON_SPRITE = Identifier.fromNamespaceAndPath(Common.MOD_ID, "icon");

    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addStatifyToPauseMenu(CallbackInfo ci) {
        List<AbstractWidget> smallButtons = new ArrayList<>();
        for (var element : this.children()) {
            if (element instanceof AbstractWidget widget) {
                if (widget.getWidth() == 20 && widget.getHeight() == 20) {
                    smallButtons.add(widget);
                }
            }
        }

        if (!smallButtons.isEmpty()) {
            int buttonWidth = 20;
            int gap = 4;
            int totalCount = smallButtons.size() + 1;
            int totalWidth = (totalCount * buttonWidth) + ((totalCount - 1) * gap);

            int currentX = (this.width - totalWidth) / 2;
            int yPos = smallButtons.getFirst().getY();

            ImageButton statifyButton = new ImageButton(
                    currentX,
                    yPos,
                    buttonWidth,
                    20,
                    VANILLA_BUTTON_SPRITES,
                    button -> {
                        if (this.minecraft.getSingleplayerServer() != null) {
                            this.minecraft.setScreenAndShow(new StatifySettingsScreen(this, this.minecraft.getSingleplayerServer().getWorldData().getLevelName()));
                        }
                    }
            ) {
                @Override
                public void extractContents(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
                    Identifier bgSprite = this.isHoveredOrFocused()
                            ? Identifier.fromNamespaceAndPath("minecraft", "widget/button_highlighted")
                            : Identifier.fromNamespaceAndPath("minecraft", "widget/button");

                    guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, bgSprite, this.getX(), this.getY(), this.width, this.height);

                    int iconSize = 12;
                    int iconX = this.getX() + (this.width - iconSize) / 2;
                    int iconY = this.getY() + (this.height - iconSize) / 2;
                    guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, STATIFY_ICON_SPRITE, iconX, iconY, iconSize, iconSize);
                }
            };

            statifyButton.setTooltip(Tooltip.create(Component.literal("Statify Settings")));

            this.addRenderableWidget(statifyButton);

            currentX += buttonWidth + gap;

            for (AbstractWidget widget : smallButtons) {
                widget.setX(currentX);
                currentX += buttonWidth + gap;
            }
        }
    }
}