package dev.soviaat.utils;

import dev.soviaat.FileManagement;
import dev.soviaat.SavedCoord;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class IconPickerScreen extends Screen {

    private final Screen parentScreen;
    private final String worldName;
    private final SavedCoord coord;
    private final List<ItemStack> availableItems = new ArrayList<>();

    private static final int ICON_SIZE = 20; // 16x16 ikon + padding
    private static final int COLS = 9;       // 9 ikon egy sorban (mint egy ládában)

    private double scrollAmount = 0;
    private int totalRows = 0;

    public IconPickerScreen(Screen parentScreen, String worldName, SavedCoord coord) {
        super(Component.literal("Select Icon - " + coord.getName()));
        this.parentScreen = parentScreen;
        this.worldName = worldName;
        this.coord = coord;

        // Összes bejegyzett item betöltése a registry-ből (levegő kivételével)
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                this.availableItems.add(new ItemStack(item));
            }
        }

        this.totalRows = (int) Math.ceil((double) availableItems.size() / COLS);
    }

    @Override
    protected void init() {
        this.clearWidgets();

        // Cancel / Vissza gomb
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> this.minecraft.setScreenAndShow(parentScreen))
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20)
                .build());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        int gridWidth = COLS * ICON_SIZE;
        int startX = (this.width - gridWidth) / 2;
        int startY = 40;
        int panelBottom = this.height - 36;
        int visibleHeight = panelBottom - startY - 10;

        if (mouseX >= startX && mouseX <= startX + gridWidth && mouseY >= startY && mouseY <= startY + visibleHeight) {
            int relX = (int) mouseX - startX;
            int relY = (int) (mouseY - startY + scrollAmount);

            int col = relX / ICON_SIZE;
            int row = relY / ICON_SIZE;
            int index = row * COLS + col;

            if (index >= 0 && index < availableItems.size()) {
                ItemStack selectedStack = availableItems.get(index);

                // 1. Frissítjük a koordináta ikonját
                coord.setItemStack(selectedStack);

                // 2. Mentés JSON-be
                FileManagement.saveCoord(this.worldName, this.coord);

                // 3. Visszatérés
                this.minecraft.setScreenAndShow(parentScreen);
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleHeight = this.height - 75;
        int totalContentHeight = totalRows * ICON_SIZE;

        if (totalContentHeight > visibleHeight) {
            int maxScroll = totalContentHeight - visibleHeight + 10;
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount - (scrollY * 16), maxScroll));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        int panelLeft = 0;
        int panelRight = this.width;
        int panelTop = 32;
        int panelBottom = this.height - 36;

        // Teljes képernyős sötétítés + panelek a CoordsScreen mintájára
        guiGraphicsExtractor.fill(0, 0, this.width, this.height, 0x80000000); // Áttetsző fekete háttér
        guiGraphicsExtractor.fill(panelLeft, panelTop, panelRight, panelBottom, 0xC0000000);
        guiGraphicsExtractor.fill(panelLeft, panelTop - 1, panelRight, panelTop, 0xFFA0A0A0);
        guiGraphicsExtractor.fill(panelLeft, panelBottom, panelRight, panelBottom + 1, 0xFFA0A0A0);

        // Cím kirajzolása
        guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);

        int gridWidth = COLS * ICON_SIZE;
        int startX = (this.width - gridWidth) / 2;
        int startY = 40;
        int visibleHeight = panelBottom - startY - 10;

        // Görgetési terület levágása (Scissor)
        guiGraphicsExtractor.enableScissor(startX - 2, startY, startX + gridWidth + 2, startY + visibleHeight);

        ItemStack hoveredStack = null;

        for (int i = 0; i < availableItems.size(); i++) {
            int row = i / COLS;
            int col = i % COLS;

            int itemX = startX + col * ICON_SIZE + 2;
            int itemY = startY + row * ICON_SIZE - (int) scrollAmount + 2;

            if (itemY + ICON_SIZE >= startY && itemY <= startY + visibleHeight) {
                // Kiemelés, ha az egér felette van
                if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16 && mouseY >= startY && mouseY <= startY + visibleHeight) {
                    guiGraphicsExtractor.fill(itemX - 2, itemY - 2, itemX + 18, itemY + 18, 0x80FFFFFF);
                    hoveredStack = availableItems.get(i);
                }

                guiGraphicsExtractor.item(availableItems.get(i), itemX, itemY);
            }
        }

        guiGraphicsExtractor.disableScissor();

        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);

        // Tooltip megjelenítése, ha az egér egy elemen áll
        if (hoveredStack != null) {
            guiGraphicsExtractor.setComponentTooltipForNextFrame(this.font, List.of(hoveredStack.getHoverName()), mouseX, mouseY);
        }
    }
}