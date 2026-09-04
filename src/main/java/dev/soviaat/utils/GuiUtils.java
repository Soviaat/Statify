package dev.soviaat.utils;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class GuiUtils {

    /**
     * Draws a panel border with rounded corners and adjustable edge lengths.
     *
     * @param extractor        GraphicsExtractor instance
     * @param left             Box left-x
     * @param top              Box top-y
     * @param right            Box right-x
     * @param bottom           Box bottom-y
     * @param thickness        Border thickness in pixels
     * @param lengthExtension  Extra length added to both ends of each border segment (0 = default)
     * @param topColor         Top border color (ARGB Hex)
     * @param leftColor        Left border color (ARGB Hex)
     * @param bottomColor      Bottom border color (ARGB Hex)
     * @param rightColor       Right border color (ARGB Hex)
     * @param offset           Distance from given rectangle's edge (positive = in, negative = out)
     */
    public static void drawPanelBorder(GuiGraphicsExtractor extractor,
                                       int left, int top, int right, int bottom,
                                       int thickness, int lengthExtension,
                                       int topColor, int leftColor, int bottomColor, int rightColor,
                                       int offset) {

        int l = left + offset;
        int t = top + offset;
        int r = right - offset;
        int b = bottom - offset;

        // Vízszintes élek hosszabbítása (l - lengthExtension és r + lengthExtension)
        extractor.fill(l + thickness - lengthExtension, t, r - thickness + lengthExtension, t + thickness, topColor);
        extractor.fill(l + thickness - lengthExtension, b - thickness, r - thickness + lengthExtension, b, bottomColor);

        // Függőleges élek hosszabbítása (t - lengthExtension és b + lengthExtension)
        extractor.fill(l, t + thickness - lengthExtension, l + thickness, b - thickness + lengthExtension, leftColor);
        extractor.fill(r - thickness, t + thickness - lengthExtension, r, b - thickness + lengthExtension, rightColor);
    }

    /**
     * Overload for standard length (lengthExtension = 0).
     */
    public static void drawPanelBorder(GuiGraphicsExtractor extractor,
                                       int left, int top, int right, int bottom,
                                       int thickness,
                                       int topColor, int leftColor, int bottomColor, int rightColor,
                                       int offset) {
        drawPanelBorder(extractor, left, top, right, bottom, thickness, 0,
                topColor, leftColor, bottomColor, rightColor, offset);
    }

    /**
     * Overload for 2-color usage (Top/Left and Bottom/Right) without length extension.
     */
    public static void drawPanelBorder(GuiGraphicsExtractor extractor,
                                       int left, int top, int right, int bottom,
                                       int thickness, int colorTopLeft, int colorBottomRight, int offset) {
        drawPanelBorder(extractor, left, top, right, bottom, thickness, 0,
                colorTopLeft, colorTopLeft, colorBottomRight, colorBottomRight, offset);
    }

    /**
     * Overload for 2-color usage with custom length extension.
     */
    public static void drawPanelBorder(GuiGraphicsExtractor extractor,
                                       int left, int top, int right, int bottom,
                                       int thickness, int lengthExtension,
                                       int colorTopLeft, int colorBottomRight, int offset) {
        drawPanelBorder(extractor, left, top, right, bottom, thickness, lengthExtension,
                colorTopLeft, colorTopLeft, colorBottomRight, colorBottomRight, offset);
    }
}