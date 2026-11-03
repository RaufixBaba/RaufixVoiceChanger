package com.raufixbaba.voicechanger.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Clean 1.21.11 text rendering helper for Raufix Voice Changer.
 */
public final class MinecraftTextAccess {
    private MinecraftTextAccess() {
    }

    public static void draw(GuiGraphics context, Font font, Component text, int x, int y, int argb) {
        context.drawString(font, text, x, y, argb);
    }

    public static void drawCentered(GuiGraphics context, Font font, Component text, int centerX, int y, int argb) {
        context.drawCenteredString(font, text, centerX, y, argb);
    }
}
