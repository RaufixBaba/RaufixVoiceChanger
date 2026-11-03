package com.raufixbaba.voicechanger.client.i18n;

import java.util.Locale;

public enum ModLanguage {
    TR("tr_tr", "TR • Türkçe"),
    EN("en_us", "EN • English"),
    AUTO("auto", "AUTO • Oyun Dili");

    private final String code;
    private final String badge;

    ModLanguage(String code, String badge) {
        this.code = code;
        this.badge = badge;
    }

    public String code() {
        return this.code;
    }

    public String badge() {
        return this.badge;
    }

    public ModLanguage next() {
        ModLanguage[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ModLanguage fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return TR;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (ModLanguage lang : values()) {
            if (lang.code.equals(normalized) || lang.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return lang;
            }
        }
        return TR;
    }
}
