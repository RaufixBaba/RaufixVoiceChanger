package com.raufixbaba.voicechanger.client.ui;

import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import com.raufixbaba.voicechanger.client.ui.compat.StudioGraphics;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

final class StudioPaint {
    static final int WHITE = RaufixTheme.TEXT_PRIMARY;
    static final int TEXT = 0xFFDCE4F7;
    static final int GREY = RaufixTheme.TEXT_MUTED;
    static final int GREEN = RaufixTheme.SUCCESS;
    static final int YELLOW = RaufixTheme.WARNING;
    static final int RED = RaufixTheme.DANGER;
    static final int BLUE = 0xFF55C2FF;
    static final int LINE_HEIGHT = 10;

    private static final int BAR_TRACK = 0xDD0B0E1A;

    private StudioPaint() {
    }

    static void bar(StudioGraphics graphics, int x, int y, int width, int height, double fraction, int color) {
        double clamped = Math.max(0.0D, Math.min(1.0D, fraction));
        graphics.fill(x, y, x + width, y + height, BAR_TRACK);
        int filled = (int) Math.round(width * clamped);
        if (filled > 0) {
            if (color == GREEN) {
                graphics.drawRgbGradientBar(x, y, filled, height, 0.92f, 0.0f);
            } else {
                graphics.fill(x, y, x + filled, y + height, color);
            }
        }
        graphics.gui().renderOutline(x, y, width, height, 0x30FFFFFF);
    }

    static void sweep(StudioGraphics graphics, int x, int y, int width, int height, long nowMillis, int color) {
        int block = Math.max(12, width / 4);
        int travel = width + block;
        int start = x - block + (int) (nowMillis % 1600L * travel / 1600L);
        graphics.fill(x, y, x + width, y + height, BAR_TRACK);
        int clLeft = Math.max(x, start);
        int clRight = Math.min(x + width, start + block);
        if (clRight > clLeft) {
            graphics.drawRgbGradientBar(clLeft, y, clRight - clLeft, height, 0.95f, 0.1f);
        }
        graphics.gui().renderOutline(x, y, width, height, 0x30FFFFFF);
    }

    static void paragraph(StudioGraphics graphics, Component text, int x, int y, int width, int maxLines, int color) {
        List<String> lines = wrap(graphics, text.getString(), width);
        for (int i = 0; i < Math.min(maxLines, lines.size()); i++) {
            String line = lines.get(i);
            if (i == maxLines - 1 && lines.size() > maxLines) {
                line(graphics, Component.literal(line + " " + lines.get(i + 1)), x, y, width, color);
            } else {
                graphics.text(Component.literal(line), x, y, color);
            }
            y += LINE_HEIGHT;
        }
    }

    static void line(StudioGraphics graphics, Component text, int x, int y, int width, int color) {
        if (graphics.textWidth(text) <= width) {
            graphics.text(text, x, y, color);
            return;
        }
        String plain = text.getString();
        while (!plain.isEmpty() && graphics.textWidth(Component.literal(plain + "…")) > width) {
            plain = plain.substring(0, plain.length() - 1);
        }
        graphics.text(Component.literal(plain + "…"), x, y, color);
    }

    private static List<String> wrap(StudioGraphics graphics, String text, int width) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            StringBuilder current = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (!current.isEmpty() && graphics.textWidth(Component.literal(candidate)) > width) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(candidate);
                }
            }
            lines.add(current.toString());
        }
        return lines;
    }
}
