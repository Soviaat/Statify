package dev.soviaat.commands;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Util.getPlatform;

public class HelpScreen extends Screen {

    private static final Identifier BACKGROUND_TEX = Identifier.fromNamespaceAndPath("minecraft", "textures/block/blue_concrete_powder.png");
    private static final Identifier WINDOW_LOCATION = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/advancements/window.png");

    private static final int WINDOW_WIDTH = 252;
    private static final int WINDOW_HEIGHT = 140;

    private static final int INNER_OFFSET_X = 9;
    private static final int INNER_OFFSET_Y = 18;
    private static final int INNER_WIDTH = 234;
    private static final int INNER_HEIGHT = 113;

    private int leftPos;
    private int topPos;
    private double scrollAmount = 0;
    private int totalContentHeight = 0;

    private final List<HelpCommandEntry> commandList = new ArrayList<>();
    private final List<StringWidget> cmdWidgets = new ArrayList<>();
    private final List<StringWidget> descWidgets = new ArrayList<>();

    private StringWidget titleWidget;
    private Button closeButton;
    private Button tutorialButton;

    public HelpScreen() {
        super(Component.literal("Statify Help"));

        commandList.add(new HelpCommandEntry("/statify help", "Displays this help GUI."));
        commandList.add(new HelpCommandEntry("/statify disable", "Disables stat collection."));
        commandList.add(new HelpCommandEntry("/statify enable", "Enables stat collection."));
        commandList.add(new HelpCommandEntry("/statify sheetid <id>", "Lets you specify the Google Sheets ID."));
        commandList.add(new HelpCommandEntry("/statify upload <on|off>", "Toggle uploading to Google Sheets."));
        commandList.add(new HelpCommandEntry("/save-coords <x> <y> <z> <name>", "Lets you save coordinates and dimension."));
        commandList.add(new HelpCommandEntry("/save-coords-here <name>", "Lets you save your position as coordinates."));
        commandList.add(new HelpCommandEntry("/get-coords", "Shows GUI of your saved coordinates."));
    }

    @Override
    protected void init() {
        this.cmdWidgets.clear();
        this.descWidgets.clear();
        this.clearWidgets();

        this.leftPos = (this.width - WINDOW_WIDTH) / 2;
        this.topPos = (this.height - WINDOW_HEIGHT) / 2;

        this.titleWidget = new StringWidget(Component.literal("Statify Help").withStyle(style -> style.withColor(0xFF404040).withShadowColor(0x00000000)), this.font);
        this.titleWidget.setPosition(this.leftPos + 12, this.topPos + 6);

        this.tutorialButton = Button.builder(Component.literal("§d[Tutorial]"), button -> {
            getPlatform().openUri("https://github.com/Soviaat/Statify#setup");
        }).bounds(this.leftPos + 260, this.topPos, 75, 16).build();
        this.tutorialButton.setTooltip(Tooltip.create(Component.literal("Opens a link to Statify tutorial.")));

        this.addRenderableWidget(this.tutorialButton);

        buildListWidgets();
        updateWidgetPositions();

        this.closeButton = Button.builder(Component.literal("Close"), btn -> this.onClose())
                .bounds(this.width / 2 - 50, this.topPos + WINDOW_HEIGHT + 6, 100, 20)
                .build();

        this.addRenderableWidget(this.closeButton);
    }

    private void buildListWidgets() {
        int currentY = 0;
        int innerX = this.leftPos + 15;

        for (HelpCommandEntry entry : commandList) {
            StringWidget cmdWidget = new StringWidget(Component.literal("§b" + entry.command), this.font);
            cmdWidget.setPosition(innerX, 0);

            StringWidget descWidget = new StringWidget(Component.literal("§7" + entry.description), this.font);
            descWidget.setPosition(innerX, 0);

            this.cmdWidgets.add(cmdWidget);
            this.descWidgets.add(descWidget);

            currentY += 26;
        }

        this.totalContentHeight = currentY;
    }

    private void updateWidgetPositions() {
        int innerY = this.topPos + INNER_OFFSET_Y;
        int currentY = innerY + 4 - (int) this.scrollAmount;

        for (int i = 0; i < commandList.size(); i++) {
            StringWidget cmdWidget = cmdWidgets.get(i);
            StringWidget descWidget = descWidgets.get(i);

            cmdWidget.setY(currentY);
            descWidget.setY(currentY + 11);

            currentY += 26;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        int innerX = this.leftPos + INNER_OFFSET_X;
        int innerY = this.topPos + INNER_OFFSET_Y;

        if (mouseX >= innerX && mouseX <= innerX + INNER_WIDTH && mouseY >= innerY && mouseY <= innerY + INNER_HEIGHT) {
            int currentY = innerY + 4 - (int) this.scrollAmount;

            for (HelpCommandEntry entry : commandList) {
                if (mouseY >= currentY && mouseY <= currentY + 24) {
                    String initialText = cleanCommand(entry.command) + " ";
                    this.minecraft.setScreenAndShow(new ChatScreen(initialText, false));
                    return true;
                }
                currentY += 26;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.totalContentHeight > INNER_HEIGHT) {
            int maxScroll = this.totalContentHeight - INNER_HEIGHT + 8;
            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount - (scrollY * 12), maxScroll));
            updateWidgetPositions();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        guiGraphicsExtractor.fill(0, 0, this.width, this.height, 0x80000000);

        int innerX = this.leftPos + INNER_OFFSET_X;
        int innerY = this.topPos + INNER_OFFSET_Y;

        guiGraphicsExtractor.blit(
                net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                WINDOW_LOCATION,
                this.leftPos,
                this.topPos,
                0.0F, 0.0F,
                WINDOW_WIDTH, WINDOW_HEIGHT,
                256, 256
        );

        guiGraphicsExtractor.enableScissor(innerX, innerY, innerX + INNER_WIDTH, innerY + INNER_HEIGHT);

        for (int x = 0; x < INNER_WIDTH; x += 16) {
            for (int y = 0; y < INNER_HEIGHT; y += 16) {
                int tileW = Math.min(16, INNER_WIDTH - x);
                int tileH = Math.min(16, INNER_HEIGHT - y);
                guiGraphicsExtractor.blit(
                        net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                        BACKGROUND_TEX,
                        innerX + x, innerY + y,
                        0.0F, 0.0F,
                        tileW, tileH,
                        16, 16
                );
            }
        }

        guiGraphicsExtractor.fill(innerX, innerY, innerX + INNER_WIDTH, innerY + INNER_HEIGHT, 0x70000000);

        if (mouseX >= innerX && mouseX <= innerX + INNER_WIDTH && mouseY >= innerY && mouseY <= innerY + INNER_HEIGHT) {
            int currentY = innerY + 4 - (int) this.scrollAmount;
            for (int i = 0; i < commandList.size(); i++) {
                if (mouseY >= currentY && mouseY <= currentY + 24) {
                    guiGraphicsExtractor.fill(innerX + 2, currentY - 2, innerX + INNER_WIDTH - 2, currentY + 22, 0x30FFFFFF);
                    break;
                }
                currentY += 26;
            }
        }

        for (int i = 0; i < cmdWidgets.size(); i++) {
            cmdWidgets.get(i).extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
            descWidgets.get(i).extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }

        if (this.totalContentHeight > INNER_HEIGHT) {
            int scrollbarWidth = 4;
            int scrollbarRight = innerX + INNER_WIDTH - 2;
            int scrollbarLeft = scrollbarRight - scrollbarWidth;

            int maxScroll = this.totalContentHeight - INNER_HEIGHT + 8;
            int thumbHeight = Math.max(16, (int) ((float) (INNER_HEIGHT * INNER_HEIGHT) / this.totalContentHeight));

            int maxThumbTop = INNER_HEIGHT - thumbHeight;
            int thumbTop = innerY + (int) ((this.scrollAmount / maxScroll) * maxThumbTop);
            int thumbBottom = thumbTop + thumbHeight;

            guiGraphicsExtractor.fill(scrollbarLeft, innerY, scrollbarRight, innerY + INNER_HEIGHT, 0xFF000000);
            guiGraphicsExtractor.fill(scrollbarLeft, thumbTop, scrollbarRight, thumbBottom, 0xFF808080);
            guiGraphicsExtractor.fill(scrollbarLeft, thumbTop, scrollbarRight - 1, thumbTop + 1, 0xFFC0C0C0);
        }

        guiGraphicsExtractor.disableScissor();

        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);

        if (this.titleWidget != null) {
            this.titleWidget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        }
    }

    private String cleanCommand(String command) {
        return command.replaceAll("<.*?>", "")
                .replaceAll("\\[.*?]", "")
                .trim();
    }

    private record HelpCommandEntry(String command, String description) {}
}