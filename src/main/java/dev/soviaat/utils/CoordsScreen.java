package dev.soviaat.utils;

import dev.soviaat.FileManagement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CoordsScreen extends Screen {

    private static final boolean ALIGN_NAME_RIGHT = true;

    private final String worldName;
    private final List<String> coordinates;
    private final List<StringWidget> nameWidgets = new ArrayList<>();
    private final List<StringWidget> coordWidgets = new ArrayList<>();
    private final List<Button> deleteButtons = new ArrayList<>();

    private StringWidget titleWidget;
    private Button closeButton;

    private double scrollAmount = 0;
    private int totalContentHeight = 0;

    public CoordsScreen(String worldName, List<String> coordinates) {
        super(Component.literal("Saved Coordinates"));
        this.worldName = worldName;
        this.coordinates = coordinates;
    }

    @Override
    protected void init() {
        this.nameWidgets.clear();
        this.coordWidgets.clear();
        this.deleteButtons.clear();
        this.clearWidgets();

        // 1. Cím
        this.titleWidget = new StringWidget(this.title, this.font);
        this.titleWidget.setPosition(this.width / 2 - this.titleWidget.getWidth() / 2, 12);

        // 2. Sorok felépítése
        int centerMargin = 15;
        int coordColumnX = this.width / 2 + centerMargin;

        if (coordinates.isEmpty()) {
            StringWidget emptyWidget = new StringWidget(Component.literal("§cNo saved coordinates!"), this.font);
            emptyWidget.setPosition(this.width / 2 - emptyWidget.getWidth() / 2, 42);
            this.addRenderableOnly(emptyWidget);
        } else {
            int currentY = 0;

            for (int i = 0; i < coordinates.size(); i++) {
                String entry = coordinates.get(i);
                String name = entry;
                String coords = "";

                if (entry.contains(" - ")) {
                    String[] parts = entry.split(" - ", 2);
                    name = parts[0];
                    coords = parts[1];
                } else if (entry.contains(": ")) {
                    String[] parts = entry.split(": ", 2);
                    name = parts[0];
                    coords = parts[1];
                }

                StringWidget nameWidget = new StringWidget(Component.literal("§f" + name), this.font);
                if (ALIGN_NAME_RIGHT) {
                    int nameRightBound = this.width / 2 - centerMargin;
                    nameWidget.setPosition(nameRightBound - nameWidget.getWidth(), 0);
                } else {
                    nameWidget.setPosition(this.width / 2 - 160, 0);
                }

                StringWidget coordWidget = new StringWidget(Component.literal("§6" + coords), this.font);
                coordWidget.setPosition(coordColumnX, 0);

                final String coordNameToDelete = name;
                int deleteBtnX = this.width / 2 + 160;

                Button deleteBtn = Button.builder(Component.literal("§c✕"), btn -> openDeleteConfirmation(coordNameToDelete, entry))
                        .bounds(deleteBtnX, 0, 16, 16)
                        .build();

                this.nameWidgets.add(nameWidget);
                this.coordWidgets.add(coordWidget);
                this.deleteButtons.add(deleteBtn);

                this.addRenderableOnly(nameWidget);
                this.addRenderableOnly(coordWidget);
                this.addRenderableWidget(deleteBtn);

                currentY += 18;
            }

            this.totalContentHeight = currentY;
        }

        updateWidgetPositions();

        this.closeButton = Button.builder(Component.literal("Let's go!"), button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
                .build();

        this.addWidget(this.closeButton);
    }

    private void openDeleteConfirmation(String coordName, String originalEntry) {
        ConfirmScreen confirmScreen = new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        FileManagement.deleteCoord(this.worldName, coordName);
                        this.coordinates.remove(originalEntry);
                    }
                    this.minecraft.setScreenAndShow(this);
                },
                Component.literal("Delete Coordinate?"),
                Component.literal("Are you sure you want to delete '§l§6" + coordName + "§r'?"),
                Component.literal("Delete"),
                Component.literal("Cancel")
        );

        this.minecraft.setScreenAndShow(confirmScreen);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleAreaHeight = (this.height - 36) - 32;

        if (this.totalContentHeight > visibleAreaHeight) {
            int maxScroll = this.totalContentHeight - visibleAreaHeight + 10;
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount - (scrollY * 12), maxScroll));
            updateWidgetPositions();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void updateWidgetPositions() {
        int startY = 42 - (int) this.scrollAmount;

        for (int i = 0; i < nameWidgets.size(); i++) {
            StringWidget nameWidget = nameWidgets.get(i);
            StringWidget coordWidget = coordWidgets.get(i);
            Button deleteBtn = deleteButtons.get(i);

            int currentY = startY + (i * 18);

            nameWidget.setY(currentY);
            coordWidget.setY(currentY);

            deleteBtn.setX(this.width / 2 + 160);
            deleteBtn.setY(currentY - 4);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        int panelLeft = 0;
        int panelRight = this.width;
        int panelTop = 32;
        int panelBottom = this.height - 36;

        guiGraphicsExtractor.fill(panelLeft, panelTop, panelRight, panelBottom, 0xC0000000);
        guiGraphicsExtractor.fill(panelLeft, panelTop - 1, panelRight, panelTop, 0xFFA0A0A0);
        guiGraphicsExtractor.fill(panelLeft, panelBottom, panelRight, panelBottom + 1, 0xFFA0A0A0);

        guiGraphicsExtractor.enableScissor(panelLeft, panelTop, panelRight, panelBottom);

        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);

        guiGraphicsExtractor.disableScissor();

        if (this.titleWidget != null) {
            this.titleWidget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }

        if (this.closeButton != null) {
            this.closeButton.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }

        int visibleAreaHeight = panelBottom - panelTop;
        if (this.totalContentHeight > visibleAreaHeight) {
            int scrollbarWidth = 6;
            int scrollbarRight = this.width - 4;
            int scrollbarLeft = scrollbarRight - scrollbarWidth;

            int maxScroll = this.totalContentHeight - visibleAreaHeight + 10;
            int thumbHeight = Math.max(32, (int) ((float) (visibleAreaHeight * visibleAreaHeight) / this.totalContentHeight));

            int maxThumbTop = visibleAreaHeight - thumbHeight;
            int thumbTop = panelTop + (int) ((this.scrollAmount / maxScroll) * maxThumbTop);
            int thumbBottom = thumbTop + thumbHeight;

            guiGraphicsExtractor.fill(scrollbarLeft, panelTop, scrollbarRight, panelBottom, 0xFF000000);
            guiGraphicsExtractor.fill(scrollbarLeft, thumbTop, scrollbarRight, thumbBottom, 0xFF808080);
            guiGraphicsExtractor.fill(scrollbarLeft, thumbTop, scrollbarRight - 1, thumbTop + 1, 0xFFC0C0C0);
            guiGraphicsExtractor.fill(scrollbarLeft, thumbTop, scrollbarLeft + 1, thumbBottom, 0xFFC0C0C0);
        }
    }
}