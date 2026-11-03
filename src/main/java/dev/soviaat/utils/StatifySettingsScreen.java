package dev.soviaat.utils;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StatifySettingsScreen extends Screen {
    private final Screen parent;
    private final String worldName;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 33);
    private final WorldSettingsManager.WorldSettings settings;

    public StatifySettingsScreen(Screen parent, String worldName) {
        super(Component.literal("Statify Settings"));
        this.parent = parent;
        this.worldName = worldName;
        this.settings = WorldSettingsManager.loadSettings(worldName);
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);

        LinearLayout buttonsRow = LinearLayout.horizontal().spacing(8);

        CycleButton<Boolean> milestoneButton = CycleButton.<Boolean>onOffBuilder(this.settings.milestones)
                .create(
                        0, 0, 150, 20,
                        Component.literal("Milestone Notifications"),
                        (CycleButton<Boolean> button, Boolean value) -> {
                            this.settings.milestones = value;
                        }
                );

        CycleButton<Boolean> sessionButton = CycleButton.<Boolean>onOffBuilder(this.settings.milestones)
                .create(
                        0, 0, 150, 20,
                        Component.literal("Last Session Notifications"),
                        (CycleButton<Boolean> button, Boolean value) -> {
                            this.settings.last_session_notifications = value;
                        }
                );

        buttonsRow.addChild(milestoneButton);
        buttonsRow.addChild(sessionButton);

        LinearLayout contents = this.layout.addToContents(LinearLayout.vertical().spacing(8));
        contents.defaultCellSetting().alignHorizontallyCenter();
        contents.addChild(buttonsRow);

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.literal("Done"), button -> {
            WorldSettingsManager.saveSettings(this.worldName, this.settings);
            this.onClose();
        }).width(200).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }
}