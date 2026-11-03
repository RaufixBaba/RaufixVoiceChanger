package com.raufixbaba.voicechanger.client.theme;

import com.raufixbaba.voicechanger.client.ui.compat.StudioGraphics;

/**
 * High-framerate animated RGB gradient & glassmorphic theme engine for
 * Raufix Voice Changer V1.0 BETA.
 */
public final class RaufixTheme {
    public static final int PANEL_BG_TOP = 0xE60B0D17;
    public static final int PANEL_BG_BOTTOM = 0xF0070810;
    public static final int CARD_BG = 0xCC121626;
    public static final int CARD_BG_HOVER = 0xE01A2038;
    public static final int CARD_BG_ACTIVE = 0xE61E2644;
    public static final int TEXT_PRIMARY = 0xFFF4F6FF;
    public static final int TEXT_SECONDARY = 0xFFB0B8D4;
    public static final int TEXT_MUTED = 0xFF7882A4;
    public static final int STATUS_ON = 0xFF38F29A;
    public static final int STATUS_WARN = 0xFFFFC847;
    public static final int STATUS_ERR = 0xFFFF5A72;
    public static final int SUCCESS = STATUS_ON;
    public static final int WARNING = STATUS_WARN;
    public static final int DANGER = STATUS_ERR;

    private static final long START_NANOS = System.nanoTime();
    private static volatile ThemePalette activePalette = ThemePalette.CHROMA_RGB;
    private static volatile boolean particlesEnabled = true;

    private RaufixTheme() {
    }

    public static ThemePalette palette() {
        return activePalette;
    }

    public static void setPalette(ThemePalette palette) {
        activePalette = palette == null ? ThemePalette.CHROMA_RGB : palette;
    }

    public static ThemePalette cyclePalette() {
        ThemePalette next = activePalette.next();
        activePalette = next;
        return next;
    }

    public static boolean particlesEnabled() {
        return particlesEnabled;
    }

    public static void setParticlesEnabled(boolean enabled) {
        particlesEnabled = enabled;
    }

    /** Elapsed animation time in seconds with sub-millisecond smoothness. */
    public static double timeSeconds() {
        return (System.nanoTime() - START_NANOS) * 1.0e-9D;
    }

    /** Smoothly pulsing factor in [0, 1]. */
    public static double pulse(double speedHz, double phaseOffset) {
        return 0.5D + 0.5D * Math.sin((timeSeconds() * speedHz + phaseOffset) * Math.PI * 2.0D);
    }

    /**
     * Returns an opaque ARGB color at the given normalized phase [0..1],
     * animated smoothly over time according to the active gradient palette.
     */
    public static int accentAt(double offset) {
        double phase = positiveFrac(offset - timeSeconds() * 0.28D);
        return switch (activePalette) {
            case CHROMA_RGB -> hsvToArgb(phase, 0.80D, 1.00D, 255);
            case CYBER_NEON -> multiStop(phase, 255,
                    0x00F5FF, 0x6A5BFF, 0xFF2ED1, 0x00D2FF);
            case CRIMSON_BLAZE -> multiStop(phase, 255,
                    0xFF2855, 0xFF6B1A, 0xFFCC29, 0xFF1E7A);
            case AURORA_MATRIX -> multiStop(phase, 255,
                    0x00FFA3, 0x00D4FF, 0x4E6BFF, 0x16F2B3);
        };
    }

    public static int rgbAt(float offset, float alpha) {
        return withAlpha(accentAt(offset), Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f));
    }

    public static int withAlpha(int rgbOrArgb, float alpha) {
        return withAlpha(rgbOrArgb, Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f));
    }

    public static int withAlpha(int rgbOrArgb, int alpha) {
        int clamped = Math.max(0, Math.min(255, alpha));
        return (clamped << 24) | (rgbOrArgb & 0x00FFFFFF);
    }

    public static int lerpColor(int c1, int c2, double t) {
        double f = Math.max(0.0D, Math.min(1.0D, t));
        int a1 = (c1 >>> 24) & 0xFF;
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int a2 = (c2 >>> 24) & 0xFF;
        int r2 = (c2 >>> 16) & 0xFF;
        int g2 = (c2 >>> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int a = (int) Math.round(a1 + (a2 - a1) * f);
        int r = (int) Math.round(r1 + (r2 - r1) * f);
        int g = (int) Math.round(g1 + (g2 - g1) * f);
        int b = (int) Math.round(b1 + (b2 - b1) * f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Paints a smooth horizontal flowing RGB gradient bar across [x, x+width]. */
    public static void fillHorizontalRgbBar(StudioGraphics g, int x, int y, int width, int height,
                                            int alpha, double phaseSpread) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int segments = Math.max(1, Math.min(48, width / 4));
        for (int i = 0; i < segments; i++) {
            int sx0 = x + (i * width) / segments;
            int sx1 = x + ((i + 1) * width) / segments;
            if (sx1 <= sx0) {
                continue;
            }
            double frac = (i + 0.5D) / segments;
            int color = withAlpha(accentAt(frac * phaseSpread), alpha);
            g.fill(sx0, y, sx1, y + height, color);
        }
    }

    /** Paints a partial horizontal RGB progress/slider fill for fraction in [0..1]. */
    public static void fillHorizontalRgbProgress(StudioGraphics g, int x, int y, int width, int height,
                                                 double fraction, int alpha) {
        double clamped = Math.max(0.0D, Math.min(1.0D, fraction));
        int fillW = (int) Math.round(width * clamped);
        if (fillW <= 0) {
            return;
        }
        int segments = Math.max(1, Math.min(36, fillW / 4));
        for (int i = 0; i < segments; i++) {
            int sx0 = x + (i * fillW) / segments;
            int sx1 = x + ((i + 1) * fillW) / segments;
            if (sx1 <= sx0) {
                continue;
            }
            double posInFull = (sx0 - x) / (double) Math.max(1, width);
            int color = withAlpha(accentAt(posInFull * 0.65D), alpha);
            g.fill(sx0, y, sx1, y + height, color);
        }
    }

    /** Draws an animated RGB gradient border around a rectangle. */
    public static void drawRgbBorder(StudioGraphics g, int x, int y, int width, int height,
                                     int alpha, double phaseOffset) {
        if (width <= 2 || height <= 2 || alpha <= 0) {
            return;
        }
        fillHorizontalRgbBar(g, x, y, width, 1, alpha, 0.55D);
        fillHorizontalRgbBar(g, x, y + height - 1, width, 1, alpha, 0.55D);
        int leftColor = withAlpha(accentAt(phaseOffset), alpha);
        int rightColor = withAlpha(accentAt(phaseOffset + 0.55D), alpha);
        g.fill(x, y + 1, x + 1, y + height - 1, leftColor);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, rightColor);
    }

    /** Draws a solid single-color border around a rectangle. */
    public static void drawSolidBorder(StudioGraphics g, int x, int y, int width, int height, int argb) {
        if (width <= 1 || height <= 1) {
            return;
        }
        g.fill(x, y, x + width, y + 1, argb);
        g.fill(x, y + height - 1, x + width, y + height, argb);
        g.fill(x, y + 1, x + 1, y + height - 1, argb);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, argb);
    }

    /** Paints the full-screen cyber studio backdrop + floating ambient neon particles. */
    public static void paintStudioBackdrop(StudioGraphics g, int width, int height, int contentTop, int contentBottom) {
        g.fillGradient(0, 0, width, height, PANEL_BG_TOP, PANEL_BG_BOTTOM);

        // Subtle top & bottom ambient RGB aura glow
        int topGlow = withAlpha(accentAt(0.10D), 32);
        int bottomGlow = withAlpha(accentAt(0.60D), 24);
        g.fillGradient(0, 0, width, Math.min(height, contentTop + 26), topGlow, 0x00000000);
        g.fillGradient(0, Math.max(0, contentBottom - 24), width, height, 0x00000000, bottomGlow);

        // Glassmorphic inner content viewport
        if (contentBottom > contentTop) {
            g.fillGradient(0, contentTop, width, contentBottom, 0x5805070E, 0x7804050B);
        }

        if (particlesEnabled) {
            paintAmbientParticles(g, width, height);
        }
    }

    private static void paintAmbientParticles(StudioGraphics g, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        double t = timeSeconds();
        int count = 22;
        for (int i = 0; i < count; i++) {
            double seed = (i + 1) * 137.508D;
            double speedY = 8.0D + (i % 5) * 4.5D;
            double baseX = ((seed * 31.7D) % width);
            double sway = Math.sin(t * (0.6D + (i % 4) * 0.25D) + i) * 14.0D;
            int px = (int) Math.floorMod(Math.round(baseX + sway), width);
            int py = (int) Math.floorMod(Math.round(height - (t * speedY + seed * 19.0D)), height);
            int size = (i % 3 == 0) ? 2 : 1;
            int alpha = (int) Math.round(28 + 46 * (0.5D + 0.5D * Math.sin(t * 1.8D + i)));
            int color = withAlpha(accentAt(i / (double) count), alpha);
            g.fill(px, py, px + size, py + size, color);
        }
    }

    /** Renders text where each character flows smoothly through the active RGB gradient. */
    public static void drawWaveText(StudioGraphics g, String text, int x, int y, double spread) {
        int cursor = x;
        int len = Math.max(1, text.length());
        for (int i = 0; i < text.length(); i++) {
            net.minecraft.network.chat.Component ch = net.minecraft.network.chat.Component.literal(
                    String.valueOf(text.charAt(i)));
            int color = accentAt((i / (double) len) * spread);
            g.text(ch, cursor, y, color);
            cursor += g.textWidth(ch);
        }
    }

    private static int multiStop(double phase, int alpha, int... stops) {
        double scaled = phase * stops.length;
        int idx = ((int) Math.floor(scaled)) % stops.length;
        int next = (idx + 1) % stops.length;
        double frac = scaled - Math.floor(scaled);
        int c1 = (alpha << 24) | (stops[idx] & 0x00FFFFFF);
        int c2 = (alpha << 24) | (stops[next] & 0x00FFFFFF);
        return lerpColor(c1, c2, frac);
    }

    private static int hsvToArgb(double h, double s, double v, int alpha) {
        double hh = positiveFrac(h) * 6.0D;
        int sector = (int) Math.floor(hh);
        double f = hh - sector;
        double p = v * (1.0D - s);
        double q = v * (1.0D - s * f);
        double t = v * (1.0D - s * (1.0D - f));
        double r;
        double g;
        double b;
        switch (sector % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        int ri = (int) Math.round(r * 255.0D);
        int gi = (int) Math.round(g * 255.0D);
        int bi = (int) Math.round(b * 255.0D);
        return (alpha << 24) | (ri << 16) | (gi << 8) | bi;
    }

    private static double positiveFrac(double value) {
        double frac = value - Math.floor(value);
        return frac < 0.0D ? frac + 1.0D : frac;
    }
}
