package com.raufixbaba.voicechanger.client.ui.compat;

import com.raufixbaba.voicechanger.client.MinecraftTextAccess;
import com.raufixbaba.voicechanger.client.audio.VoiceDiagnostics;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Animated RGB Neon Studio drawing engine for Raufix Voice Changer V1.0 BETA.
 */
public final class StudioGraphics {
    private static final int GRADIENT_SEGMENTS = 24;

    private final GuiGraphics delegate;
    private final Font font;

    public StudioGraphics(GuiGraphics delegate, Font font) {
        this.delegate = delegate;
        this.font = font;
    }

    public GuiGraphics gui() {
        return this.delegate;
    }

    public Font font() {
        return this.font;
    }

    public void fill(int left, int top, int right, int bottom, int argb) {
        this.delegate.fill(left, top, right, bottom, argb);
    }

    public void fillGradient(int left, int top, int right, int bottom, int topArgb, int bottomArgb) {
        this.delegate.fillGradient(left, top, right, bottom, topArgb, bottomArgb);
    }

    public void text(Component text, int x, int y, int argb) {
        MinecraftTextAccess.draw(this.delegate, this.font, text, x, y, argb);
    }

    public void centeredText(Component text, int centerX, int y, int argb) {
        MinecraftTextAccess.drawCentered(this.delegate, this.font, text, centerX, y, argb);
    }

    public int textWidth(Component text) {
        return this.font.width(text);
    }

    public int lineHeight() {
        return this.font.lineHeight;
    }

    /** Full-screen deep cyber-glass backdrop with subtle animated RGB aurora bars & scanlines. */
    public void drawCyberBackdrop(int width, int height) {
        this.delegate.fillGradient(0, 0, width, height, 0xEE060711, 0xF40C0F1E);

        // Subtle futuristic horizontal scanlines
        for (int y = 0; y < height; y += 6) {
            this.delegate.fill(0, y, width, y + 1, 0x09FFFFFF);
        }

        // Top & bottom animated RGB aurora accent strips
        drawRgbGradientBar(0, 0, width, 2, 0.95f, 0.0f);
        drawRgbGradientBar(0, 2, width, 2, 0.22f, 0.0f);
        drawRgbGradientBar(0, height - 2, width, 2, 0.85f, 0.5f);
    }

    /** Smooth multi-segment flowing RGB gradient bar. */
    public void drawRgbGradientBar(int x, int y, int width, int height, float alpha, float phaseOffset) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int segments = Math.max(1, Math.min(GRADIENT_SEGMENTS, width / 4));
        for (int i = 0; i < segments; i++) {
            int segStart = x + (i * width) / segments;
            int segEnd = x + ((i + 1) * width) / segments;
            if (segEnd <= segStart) {
                continue;
            }
            float offset = phaseOffset + (i / (float) segments) * 0.85f;
            int color = RaufixTheme.rgbAt(offset, alpha);
            this.delegate.fill(segStart, y, segEnd, y + height, color);
        }
    }

    /** Animated 1px RGB border with optional soft outer neon glow. */
    public void drawRgbBorder(int x, int y, int width, int height, float alpha, float phaseOffset, boolean outerGlow) {
        if (outerGlow) {
            float glowAlpha = alpha * 0.22f * (float) RaufixTheme.pulse(0.65f, 1.0f);
            drawRgbGradientBar(x - 1, y - 1, width + 2, 1, glowAlpha, phaseOffset);
            drawRgbGradientBar(x - 1, y + height, width + 2, 1, glowAlpha, phaseOffset + 0.5f);
            int leftGlow = RaufixTheme.rgbAt(phaseOffset, glowAlpha);
            int rightGlow = RaufixTheme.rgbAt(phaseOffset + 0.85f, glowAlpha);
            this.delegate.fill(x - 1, y, x, y + height, leftGlow);
            this.delegate.fill(x + width, y, x + width + 1, y + height, rightGlow);
        }

        drawRgbGradientBar(x, y, width, 1, alpha, phaseOffset);
        drawRgbGradientBar(x, y + height - 1, width, 1, alpha, phaseOffset + 0.45f);

        int vSegments = Math.max(1, Math.min(8, height / 6));
        for (int i = 0; i < vSegments; i++) {
            int sy = y + (i * height) / vSegments;
            int ey = y + ((i + 1) * height) / vSegments;
            float frac = i / (float) vSegments;
            this.delegate.fill(x, sy, x + 1, ey, RaufixTheme.rgbAt(phaseOffset + frac * 0.45f, alpha));
            this.delegate.fill(x + width - 1, sy, x + width, ey,
                    RaufixTheme.rgbAt(phaseOffset + 0.85f - frac * 0.45f, alpha));
        }
    }

    /** Frosted dark obsidian panel with animated RGB border. */
    public void drawNeonPanel(int x, int y, int width, int height, boolean brightGlow, float phaseOffset) {
        this.delegate.fillGradient(x, y, x + width, y + height, 0xEC0E1222, 0xF2090B15);
        this.delegate.fill(x + 1, y + 1, x + width - 1, y + 2, 0x16FFFFFF);
        drawRgbBorder(x, y, width, height, brightGlow ? 0.92f : 0.48f, phaseOffset, brightGlow);
    }

    /** Crisp pill badge (e.g. V1 BETA, YENİ, AKTİF). */
    public int drawPillBadge(Component label, int x, int y, int bgArgb, int borderArgb, int textArgb) {
        int padX = 5;
        int w = textWidth(label) + padX * 2;
        int h = 12;
        this.delegate.fill(x, y, x + w, y + h, bgArgb);
        this.delegate.renderOutline(x, y, w, h, borderArgb);
        text(label, x + padX, y + 2, textArgb);
        return w;
    }

    /** Live 16-bar animated RGB vocal spectrum analyzer. */
    public void drawSpectrumVisualizer(int x, int y, int width, int height,
                                       VoiceDiagnostics diag, double micPeak, boolean active) {
        this.delegate.fillGradient(x, y, x + width, y + height, 0xD8060811, 0xEA0B0F1D);
        this.delegate.renderOutline(x, y, width, height, 0x35FFFFFF);

        int bars = 16;
        int pad = 4;
        int innerW = Math.max(16, width - pad * 2);
        int innerH = Math.max(6, height - 6);
        int gap = 2;
        int barW = Math.max(2, (innerW - gap * (bars - 1)) / bars);
        int totalW = barW * bars + gap * (bars - 1);
        int startX = x + (width - totalW) / 2;
        int bottomY = y + height - 3;

        double timeSec = (System.currentTimeMillis() % 60_000L) / 1000.0D;
        for (int i = 0; i < bars; i++) {
            int bandIdx = i / 2;
            float liveBand = diag != null ? diag.band(bandIdx) : 0f;
            float peakBoost = (float) Math.min(1.0D, micPeak * 2.4D);
            float level = Math.max(liveBand, peakBoost * (0.35f + 0.65f * (float) Math.sin((i + 1) * 0.7D + timeSec * 7.0D) * 0.5f + 0.5f));

            float idleWave = active
                    ? 0.14f + 0.10f * (float) (0.5D + 0.5D * Math.sin(timeSec * 3.6D + i * 0.45D))
                    : 0.06f;
            float finalFrac = Math.max(idleWave, Math.min(1.0f, level));
            int barH = Math.max(2, Math.round(innerH * finalFrac));
            int bx = startX + i * (barW + gap);

            int topColor = active ? RaufixTheme.rgbAt(i * 0.055f, 0.96f) : 0xFF3E4660;
            int botColor = active ? RaufixTheme.rgbAt(i * 0.055f + 0.25f, 0.55f) : 0xFF232838;
            this.delegate.fillGradient(bx, bottomY - barH, bx + barW, bottomY, topColor, botColor);
        }
    }
}
