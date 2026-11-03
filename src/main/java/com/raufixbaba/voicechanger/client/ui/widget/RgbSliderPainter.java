package com.raufixbaba.voicechanger.client.ui.widget;

import com.raufixbaba.voicechanger.client.MinecraftTextAccess;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Shared animated RGB Neon slider renderer used by StrengthSlider, ParameterSlider, and RangeSlider.
 */
final class RgbSliderPainter {
    private RgbSliderPainter() {
    }

    static void paint(GuiGraphics g, int x, int y, int w, int h, double value,
                      boolean active, boolean hovered, Component message) {
        double clamped = Math.max(0.0D, Math.min(1.0D, value));
        float phase = ((y * 13 + x * 7) & 0xFF) / 255.0f;

        // Cyber-glass track
        g.fillGradient(x, y, x + w, y + h, 0xEB0C101E, 0xF2080B14);

        // Filled RGB gradient portion
        int fillW = (int) Math.round((w - 2) * clamped);
        if (fillW > 0) {
            int segs = Math.max(1, Math.min(16, fillW / 6));
            for (int i = 0; i < segs; i++) {
                int sx = x + 1 + (i * fillW) / segs;
                int ex = x + 1 + ((i + 1) * fillW) / segs;
                int col = active
                        ? RaufixTheme.rgbAt(phase + i * 0.045f, hovered ? 0.52f : 0.38f)
                        : 0x443B4258;
                g.fill(sx, y + 1, ex, y + h - 1, col);
            }
            // Bright bottom neon progress line
            for (int i = 0; i < segs; i++) {
                int sx = x + 1 + (i * fillW) / segs;
                int ex = x + 1 + ((i + 1) * fillW) / segs;
                int col = active
                        ? RaufixTheme.rgbAt(phase + i * 0.045f, 0.95f)
                        : 0x88525A75;
                g.fill(sx, y + h - 2, ex, y + h - 1, col);
            }
        }

        // Handle thumb
        int thumbW = 6;
        int thumbX = x + 1 + (int) Math.round((w - 2 - thumbW) * clamped);
        int thumbCol = active ? RaufixTheme.rgbAt(phase + (float) clamped * 0.5f, 1.0f) : 0xFF5D657E;
        g.fill(thumbX, y + 1, thumbX + thumbW, y + h - 1, thumbCol);
        g.fill(thumbX + 1, y + 2, thumbX + thumbW - 1, y + h - 2, 0xCCFFFFFF);

        // Border
        int border = !active ? 0x28FFFFFF : (hovered ? RaufixTheme.rgbAt(phase, 0.88f) : 0x4298A8D0);
        g.renderOutline(x, y, w, h, border);

        // Label
        Font font = Minecraft.getInstance().font;
        Component label = message;
        int maxW = Math.max(16, w - 10);
        if (font.width(label) > maxW) {
            String plain = label.getString();
            while (plain.length() > 1 && font.width(plain + "…") > maxW) {
                plain = plain.substring(0, plain.length() - 1);
            }
            label = Component.literal(plain + "…");
        }
        int textColor = active ? 0xFFF6F9FF : 0xFF6E768E;
        MinecraftTextAccess.drawCentered(g, font, label, x + w / 2, y + (h - 8) / 2, textColor);
    }
}
