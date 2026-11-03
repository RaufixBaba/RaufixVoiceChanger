package com.raufixbaba.voicechanger.client.model;

public enum VoiceCategory {
    ALL("all", "raufixvc.category.all", 0xFF00F0FF),
    REALISTIC("realistic", "raufixvc.category.realistic", 0xFFFF4DA6),
    CHARACTER("character", "raufixvc.category.character", 0xFF9B6BFF),
    FANTASY("fantasy", "raufixvc.category.fantasy", 0xFFFF5252),
    EFFECTS("effects", "raufixvc.category.effects", 0xFF28F0B4),
    SAVED("saved", "raufixvc.category.saved", 0xFFFFC847);

    private final String id;
    private final String translationKey;
    private final int accentColor;

    VoiceCategory(String id, String translationKey, int accentColor) {
        this.id = id;
        this.translationKey = translationKey;
        this.accentColor = accentColor;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public int accentColor() {
        return this.accentColor;
    }
}
