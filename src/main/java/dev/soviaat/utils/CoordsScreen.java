package dev.soviaat.utils;

import dev.soviaat.Common;
import dev.soviaat.FileManagement;
import dev.soviaat.SavedCoord;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.*;
import java.util.stream.Collectors;

public class CoordsScreen extends Screen {
    WidgetSprites EDIT_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Common.MOD_ID, "edit"),
            Identifier.fromNamespaceAndPath(Common.MOD_ID, "edit-hover")
    );
    private final String worldName;
    private final List<SavedCoord> coordinates;

    private String activeFilter = null;

    private final List<StringWidget> nameWidgets = new ArrayList<>();
    private final List<StringWidget> coordWidgets = new ArrayList<>();
    private final List<Button> settingsButtons = new ArrayList<>();
    private final List<StringWidget> categoryHeaderWidgets = new ArrayList<>();

    private StringWidget titleWidget;
    private Button closeButton;
    private Button filterButton;

    private double scrollAmount = 0;
    private int totalContentHeight = 0;

    private final List<SavedCoord> displayedCoords = new ArrayList<>();

    public CoordsScreen(String worldName, List<SavedCoord> coordinates) {
        super(Component.literal("Saved Coordinates"));
        this.worldName = worldName;
        this.coordinates = coordinates;
    }

    private String normalizeDim(String dim) {
        if (dim == null) return "minecraft:overworld";

        String cleanDim = dim.toLowerCase().trim();

        return switch (cleanDim) {
            case "overworld", "minecraft:overworld" -> "minecraft:overworld";
            case "nether", "the_nether", "minecraft:the_nether" -> "minecraft:the_nether";
            case "end", "the_end", "minecraft:the_end" -> "minecraft:the_end";
            default -> cleanDim;
        };
    }

    @Override
    protected void init() {
        this.nameWidgets.clear();
        this.coordWidgets.clear();
        this.settingsButtons.clear();
        this.categoryHeaderWidgets.clear();
        this.displayedCoords.clear();
        this.clearWidgets();

        this.titleWidget = new StringWidget(this.title, this.font);
        this.titleWidget.setPosition(this.width / 2 - this.titleWidget.getWidth() / 2, 10);

        String filterText = "Filter: " + (activeFilter == null ? "All" : getShortDimName(activeFilter));
        this.filterButton = Button.builder(Component.literal("§e" + filterText), btn -> openFilterPopup())
                .bounds(this.width - 110, 8, 100, 20)
                .build();

        this.addRenderableWidget(this.filterButton);

        if (coordinates.isEmpty()) {
            StringWidget emptyWidget = new StringWidget(Component.literal("§cNo saved coordinates!"), this.font);
            emptyWidget.setPosition(this.width / 2 - emptyWidget.getWidth() / 2, 42);
            this.addRenderableOnly(emptyWidget);
        } else {
            buildCoordinateList();
        }

        updateWidgetPositions();

        this.closeButton = Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
                .build();

        this.addRenderableWidget(this.closeButton);
    }

    private void buildCoordinateList() {
        int currentY = 0;

        if (activeFilter != null) {
            List<SavedCoord> filtered = coordinates.stream()
                    .filter(c -> normalizeDim(c.getDimension()).equals(activeFilter))
                    .toList();

            for (SavedCoord coordObj : filtered) {
                currentY = addCoordRow(coordObj, currentY);
            }
        } else {
            List<String> dimOrder = List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
            Map<String, List<SavedCoord>> grouped = coordinates.stream()
                    .collect(Collectors.groupingBy(c -> normalizeDim(c.getDimension())));

            for (String dim : dimOrder) {
                if (grouped.containsKey(dim) && !grouped.get(dim).isEmpty()) {
                    currentY = addCategoryHeader(getDimTitle(dim), currentY);
                    for (SavedCoord coordObj : grouped.get(dim)) {
                        currentY = addCoordRow(coordObj, currentY);
                    }
                }
            }

            for (Map.Entry<String, List<SavedCoord>> entry : grouped.entrySet()) {
                if (!dimOrder.contains(entry.getKey()) && !entry.getValue().isEmpty()) {
                    currentY = addCategoryHeader(getDimTitle(entry.getKey()), currentY);
                    for (SavedCoord coordObj : entry.getValue()) {
                        currentY = addCoordRow(coordObj, currentY);
                    }
                }
            }
        }

        this.totalContentHeight = currentY;
    }

    private int addCategoryHeader(String title, int currentY) {
        StringWidget header = new StringWidget(Component.literal("§r§l─── " + title + "§r ───"), this.font);
        header.setPosition(this.width / 2 - header.getWidth() / 2, 0);
        this.categoryHeaderWidgets.add(header);
        this.addRenderableOnly(header);
        return currentY + 18;
    }

    private int addCoordRow(SavedCoord coordObj, int currentY) {
        displayedCoords.add(coordObj);

        StringWidget nameWidget = new StringWidget(Component.literal("§f" + coordObj.getName()), this.font);
        int nameRightBound = this.width / 2 - 35;
        nameWidget.setPosition(nameRightBound - nameWidget.getWidth(), 0);

        StringWidget coordWidget = new StringWidget(Component.literal("§6" + coordObj.getCoordsText()), this.font);
        coordWidget.setPosition(this.width / 2 + 15, 0);

        Button settingsBtn = new ImageButton(
                this.width / 2 + 115, 0,
                14, 14,
                EDIT_SPRITES,
                btn -> openCoordSettings(coordObj)
        );

        settingsBtn.setTooltip(Tooltip.create(Component.literal("Edit Coordinates")));

        this.nameWidgets.add(nameWidget);
        this.coordWidgets.add(coordWidget);
        this.settingsButtons.add(settingsBtn);

        this.addRenderableOnly(nameWidget);
        this.addRenderableOnly(coordWidget);
        this.addRenderableWidget(settingsBtn);

        return currentY + 22;
    }

    private void openCoordSettings(SavedCoord coordObj) {
        this.minecraft.setScreenAndShow(new CoordSettingsScreen(this, this.worldName, coordObj));
    }

    private void openFilterPopup() {
        Set<String> availableDims = coordinates.stream()
                .map(c -> normalizeDim(c.getDimension()))
                .collect(Collectors.toSet());

        this.minecraft.setScreenAndShow(new FilterPopupScreen(this, availableDims, activeFilter, selected -> {
            this.activeFilter = selected;
            this.init();
        }));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        int panelTop = 32;
        int panelBottom = this.height - 36;

        if (mouseY >= panelTop && mouseY <= panelBottom) {
            for (int i = 0; i < displayedCoords.size(); i++) {
                SavedCoord coord = displayedCoords.get(i);
                if (i < settingsButtons.size()) {
                    int rowY = settingsButtons.get(i).getY();
                    int iconX = this.width / 2 - 28;

                    if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= rowY && mouseY <= rowY + 16) {
                        this.minecraft.setScreenAndShow(new IconPickerScreen(this, this.worldName, coord));
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
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
        int panelTop = 32;
        int panelBottom = this.height - 36;

        int currentY = startY;

        if (activeFilter == null) {
            List<String> dimOrder = List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
            Map<String, List<SavedCoord>> grouped = coordinates.stream()
                    .collect(Collectors.groupingBy(c -> normalizeDim(c.getDimension())));

            int headerIdx = 0;
            int coordIndex = 0;

            for (String dim : dimOrder) {
                if (grouped.containsKey(dim) && !grouped.get(dim).isEmpty()) {
                    if (headerIdx < categoryHeaderWidgets.size()) {
                        StringWidget header = categoryHeaderWidgets.get(headerIdx++);
                        header.setY(currentY + 2);
                        header.visible = currentY >= panelTop - 10 && currentY <= panelBottom;
                    }
                    currentY += 18;

                    for (SavedCoord coord : grouped.get(dim)) {
                        currentY = updateRowPosition(coordIndex++, currentY, panelTop, panelBottom);
                    }
                }
            }
        } else {
            for (int i = 0; i < displayedCoords.size(); i++) {
                currentY = updateRowPosition(i, currentY, panelTop, panelBottom);
            }
        }
    }

    private int updateRowPosition(int index, int currentY, int panelTop, int panelBottom) {
        if (index < nameWidgets.size()) {
            boolean visible = currentY >= panelTop - 5 && currentY + 16 <= panelBottom + 5;

            StringWidget nameWidget = nameWidgets.get(index);
            StringWidget coordWidget = coordWidgets.get(index);
            Button settingsBtn = settingsButtons.get(index);

            nameWidget.setY(currentY + 3);
            coordWidget.setY(currentY + 3);
            settingsBtn.setY(currentY);

            nameWidget.visible = visible;
            coordWidget.visible = visible;
            settingsBtn.visible = visible;
        }
        return currentY + 22;
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

        for (int i = 0; i < displayedCoords.size(); i++) {
            if (i < settingsButtons.size()) {
                SavedCoord coord = displayedCoords.get(i);
                int currentY = settingsButtons.get(i).getY();
                int iconX = this.width / 2 - 28;

                if (currentY >= panelTop - 16 && currentY <= panelBottom) {
                    guiGraphicsExtractor.item(coord.getItemStack(), iconX, currentY);
                }
            }
        }

        guiGraphicsExtractor.disableScissor();

        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);

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

    private String getShortDimName(String dim) {
        String normalized = normalizeDim(dim);
        return switch (normalized) {
            case "minecraft:overworld" -> "§aOW";
            case "minecraft:the_nether" -> "§cNether";
            case "minecraft:the_end" -> "§dEnd";
            default -> "§7Other";
        };
    }

    private String getDimTitle(String dim) {
        String normalized = normalizeDim(dim);
        return switch (normalized) {
            case "minecraft:overworld" -> "§aOverworld";
            case "minecraft:the_nether" -> "§cNether";
            case "minecraft:the_end" -> "§dThe End";
            default -> "§7Other";
        };
    }

    private static class FilterPopupScreen extends Screen {

        private final Screen parent;
        private final Set<String> availableDims;
        private final String currentFilter;
        private final java.util.function.Consumer<String> onSelect;

        protected FilterPopupScreen(Screen parent, Set<String> availableDims, String currentFilter, java.util.function.Consumer<String> onSelect) {
            super(Component.literal("Filter Dimensions"));
            this.parent = parent;
            this.availableDims = availableDims;
            this.currentFilter = currentFilter;
            this.onSelect = onSelect;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int startY = this.height / 2 - 50;

            this.addRenderableWidget(Button.builder(Component.literal(currentFilter == null ? "§a✔ Show All" : "Show All"), btn -> {
                onSelect.accept(null);
                this.minecraft.setScreenAndShow(parent);
            }).bounds(centerX - 60, startY, 120, 20).build());

            int yOffset = startY + 24;

            if (availableDims.contains("minecraft:overworld")) {
                boolean active = "minecraft:overworld".equals(currentFilter);
                this.addRenderableWidget(Button.builder(Component.literal(active ? "§a✔ Overworld" : "Overworld"), btn -> select("minecraft:overworld"))
                        .bounds(centerX - 60, yOffset, 120, 20).build());
                yOffset += 24;
            }

            if (availableDims.contains("minecraft:the_nether")) {
                boolean active = "minecraft:the_nether".equals(currentFilter);
                this.addRenderableWidget(Button.builder(Component.literal(active ? "§a✔ Nether" : "Nether"), btn -> select("minecraft:the_nether"))
                        .bounds(centerX - 60, yOffset, 120, 20).build());
                yOffset += 24;
            }

            if (availableDims.contains("minecraft:the_end")) {
                boolean active = "minecraft:the_end".equals(currentFilter);
                this.addRenderableWidget(Button.builder(Component.literal(active ? "§a✔ The End" : "The End"), btn -> select("minecraft:the_end"))
                        .bounds(centerX - 60, yOffset, 120, 20).build());
                yOffset += 24;
            }

            this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> this.minecraft.setScreenAndShow(parent))
                    .bounds(centerX - 40, yOffset + 10, 80, 20).build());
        }

        private void select(String dim) {
            onSelect.accept(dim);
            this.minecraft.setScreenAndShow(parent);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
            guiGraphicsExtractor.fill(0, 0, this.width, this.height, 0x80000000);
            int boxWidth = 150;
            int boxHeight = 160;
            guiGraphicsExtractor.fill(this.width / 2 - boxWidth / 2, this.height / 2 - 70, this.width / 2 + boxWidth / 2, this.height / 2 + boxHeight / 2, 0xF0101010);

            super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }
    }

    private static class CoordSettingsScreen extends Screen {

        private final Screen parent;
        private final String worldName;
        private final SavedCoord coord;

        private EditBox nameBox;
        private EditBox xBox;
        private EditBox yBox;
        private EditBox zBox;

        private String currentDim;

        protected CoordSettingsScreen(Screen parent, String worldName, SavedCoord coord) {
            super(Component.literal("Edit Coordinate"));
            this.parent = parent;
            this.worldName = worldName;
            this.coord = coord;
            this.currentDim = coord.getDimension();
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int startY = this.height / 2 - 80;

            this.nameBox = new EditBox(this.font, centerX - 80, startY, 160, 18, Component.literal("Name"));
            this.nameBox.setValue(coord.getName());
            this.nameBox.setMaxLength(40);
            this.addRenderableWidget(this.nameBox);

            int coordY = startY + 24;
            this.xBox = new EditBox(this.font, centerX - 80, coordY, 50, 18, Component.literal("X"));
            this.xBox.setValue(String.valueOf(coord.getX()));
            this.xBox.setMaxLength(9);
            this.addRenderableWidget(this.xBox);

            this.yBox = new EditBox(this.font, centerX - 25, coordY, 50, 18, Component.literal("Y"));
            this.yBox.setValue(String.valueOf(coord.getY()));
            this.yBox.setMaxLength(3);
            this.addRenderableWidget(this.yBox);

            this.zBox = new EditBox(this.font, centerX + 30, coordY, 50, 18, Component.literal("Z"));
            this.zBox.setValue(String.valueOf(coord.getZ()));
            this.zBox.setMaxLength(9);
            this.addRenderableWidget(this.zBox);

            int btnY = coordY + 24;
            Button dimBtn = Button.builder(Component.literal("Dim: " + getDimLabel(currentDim)), btn -> {
                cycleDim();
                btn.setMessage(Component.literal("Dim: " + getDimLabel(currentDim)));
            }).bounds(centerX - 80, btnY, 160, 20).build();
            this.addRenderableWidget(dimBtn);

            btnY += 24;
            Button iconBtn = Button.builder(Component.literal("Change Icon"), btn -> {
                this.minecraft.setScreenAndShow(new IconPickerScreen(this, this.worldName, coord));
            }).bounds(centerX - 80, btnY, 160, 20).build();
            this.addRenderableWidget(iconBtn);

            btnY += 24;
            Button saveBtn = Button.builder(Component.literal("§aSave"), btn -> saveChanges())
                    .bounds(centerX - 80, btnY, 76, 20).build();
            Button deleteBtn = Button.builder(Component.literal("§cDelete"), btn -> openDeleteConfirm())
                    .bounds(centerX + 4, btnY, 76, 20).build();

            this.addRenderableWidget(saveBtn);
            this.addRenderableWidget(deleteBtn);

            btnY += 24;
            Button cancelBtn = Button.builder(Component.literal("Cancel"), btn -> this.minecraft.setScreenAndShow(parent))
                    .bounds(centerX - 40, btnY, 80, 20).build();
            this.addRenderableWidget(cancelBtn);
        }

        private void cycleDim() {
            if (currentDim.contains("overworld")) {
                currentDim = "minecraft:the_nether";
            } else if (currentDim.contains("nether")) {
                currentDim = "minecraft:the_end";
            } else {
                currentDim = "minecraft:overworld";
            }
        }

        private String getDimLabel(String dim) {
            if (dim.contains("nether")) return "§cNether";
            if (dim.contains("end")) return "§dThe End";
            return "§aOverworld";
        }

        private void saveChanges() {
            try {
                String oldName = coord.getName();
                String newName = nameBox.getValue().trim();
                int newX = Integer.parseInt(xBox.getValue().trim());
                int newY = Integer.parseInt(yBox.getValue().trim());
                int newZ = Integer.parseInt(zBox.getValue().trim());

                if (!oldName.equalsIgnoreCase(newName)) {
                    FileManagement.deleteCoord(worldName, oldName);
                }

                coord.setName(newName);
                coord.setX(newX);
                coord.setY(newY);
                coord.setZ(newZ);
                coord.setDimension(currentDim);

                FileManagement.saveCoord(worldName, coord);
            } catch (Exception ignored) {
            }

            this.minecraft.setScreenAndShow(parent);
        }

        private void openDeleteConfirm() {
            ConfirmScreen confirmScreen = new ConfirmScreen(
                    confirmed -> {
                        if (confirmed) {
                            FileManagement.deleteCoord(worldName, coord.getName());
                            if (parent instanceof CoordsScreen cs) {
                                cs.coordinates.remove(coord);
                            }
                        }
                        this.minecraft.setScreenAndShow(parent);
                    },
                    Component.literal("Delete Coordinate?"),
                    Component.literal("Are you sure you want to delete '§l§6" + coord.getName() + "§r'?"),
                    Component.literal("Delete"),
                    Component.literal("Cancel")
            );
            this.minecraft.setScreenAndShow(confirmScreen);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
            guiGraphicsExtractor.fill(0, 0, this.width, this.height, 0x80000000);

            int boxWidth = 180;
            int boxHeight = 210;
            int left = this.width / 2 - boxWidth / 2;
            int top = this.height / 2 - 95;
            int right = left + boxWidth;
            int bottom = top + boxHeight;

            guiGraphicsExtractor.fill(left + 2, top + 2, right - 2, bottom - 2, 0xF0101010);

            GuiUtils.drawPanelBorder(
                    guiGraphicsExtractor, left, top, right, bottom, 1,
                    0, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF373737, 0xFF373737,
                    1
            );

            super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }
    }
}