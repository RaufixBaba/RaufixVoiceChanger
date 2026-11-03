package com.raufixbaba.voicechanger.client.theme;

import java.util.Locale;

public enum ThemePalette {
    CHROMA_RGB("chroma_rgb", "raufixvc.theme.chroma_rgb"),
    CYBER_NEON("cyber_neon", "raufixvc.theme.cyber_neon"),
    CRIMSON_BLAZE("crimson_blaze", "raufixvc.theme.crimson_blaze"),
    AURORA_MATRIX("aurora_matrix", "raufixvc.theme.aurora_matrix");

    private final String id;
    private final String translationKey;

    ThemePalette(String id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public ThemePalette next() {
        ThemePalette[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public static ThemePalette fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            return CHROMA_RGB;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (ThemePalette palette : values()) {
            if (palette.id.equals(normalized) || palette.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return palette;
            }
        }
        return CHROMA_RGB;
    }
}
