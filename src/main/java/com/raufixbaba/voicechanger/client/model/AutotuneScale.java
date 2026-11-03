package com.raufixbaba.voicechanger.client.model;

public enum AutotuneScale {
    CHROMATIC("chromatic", new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}),
    MAJOR("major", new int[]{0, 2, 4, 5, 7, 9, 11}),
    MINOR("minor", new int[]{0, 2, 3, 5, 7, 8, 10});

    private static final AutotuneScale[] VALUES = values();

    private final String key;
    private final int[] degrees;

    AutotuneScale(String key, int[] degrees) {
        this.key = key;
        this.degrees = degrees;
    }

    public String translationKey() {
        return "raufixvc.autotune.scale." + this.key;
    }

    public int[] degrees() {
        return this.degrees.clone();
    }

    public int[] semitones() {
        return this.degrees.clone();
    }

    public static AutotuneScale byIndex(int index) {
        int clamped = Math.max(0, Math.min(VALUES.length - 1, index));
        return VALUES[clamped];
    }
}
