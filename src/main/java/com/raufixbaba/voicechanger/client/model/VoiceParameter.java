package com.raufixbaba.voicechanger.client.model;

import java.util.Locale;

public enum VoiceParameter {
    // --- Main ---
    MIX("mix", Unit.PERCENT, 0.0D, 1.0D, 1.0D, 0.0D),
    GAIN("gain", Unit.PERCENT, 0.25D, 2.50D, 1.0D, 1.0D),

    // --- Voice Identity ---
    PITCH("pitch", Unit.SEMITONES, 0.50D, 2.00D, 1.0D, 1.0D),
    FORMANT("formant", Unit.PERCENT, 0.50D, 2.00D, 1.0D, 1.0D),
    VOICE_MATCH("voiceMatch", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    GROWL("growl", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),

    // --- Studio Vocal Realism (NEW in Raufix V1 BETA) ---
    BREATHINESS("breathiness", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    WARMTH("warmth", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    CLARITY("clarity", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    CHORUS("chorus", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),

    // --- Tone ---
    LOW_EQ("lowEq", Unit.DECIBELS, -12.0D, 12.0D, 0.0D, 0.0D),
    MID_EQ("midEq", Unit.DECIBELS, -12.0D, 12.0D, 0.0D, 0.0D),
    HIGH_EQ("highEq", Unit.DECIBELS, -12.0D, 12.0D, 0.0D, 0.0D),
    RADIO("radio", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),

    // --- Cleanup ---
    GATE("gate", Unit.PERCENT, 0.0D, 1.0D, 0.30D, 0.0D),
    DE_ESS("deEss", Unit.PERCENT, 0.0D, 1.0D, 0.30D, 0.0D),

    // --- Character ---
    DISTORTION("distortion", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    BIT_DEPTH("bitDepth", Unit.BITS, 1.0D, 16.0D, 16.0D, 16.0D, true),
    NOISE("noise", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    ROBOT_MIX("robotMix", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    ROBOT_FREQUENCY("robotFrequency", Unit.HERTZ, 20.0D, 300.0D, 60.0D, 60.0D, true),
    TREMOLO_DEPTH("tremoloDepth", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    TREMOLO_RATE("tremoloRate", Unit.HERTZ, 0.20D, 12.0D, 2.50D, 2.50D),

    // --- Space ---
    REVERB_MIX("reverbMix", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    REVERB_SIZE("reverbSize", Unit.PERCENT, 0.0D, 1.0D, 0.40D, 0.40D),
    REVERB_DECAY("reverbDecay", Unit.PERCENT, 0.0D, 1.0D, 0.50D, 0.50D),

    // --- Autotune ---
    AUTOTUNE_MIX("autotuneMix", Unit.PERCENT, 0.0D, 1.0D, 0.0D, 0.0D),
    AUTOTUNE_SPEED("autotuneStrength", Unit.PERCENT, 0.0D, 1.0D, 0.60D, 0.60D),
    AUTOTUNE_KEY("autotuneKey", Unit.PLAIN, 0.0D, 11.0D, 0.0D, 0.0D, true),
    AUTOTUNE_SCALE("autotuneScale", Unit.PLAIN, 0.0D, 2.0D, 0.0D, 0.0D, true);

    public enum Unit {
        PLAIN,
        PERCENT,
        DECIBELS,
        SEMITONES,
        HERTZ,
        BITS;

        public String format(double value) {
            return switch (this) {
                case PERCENT -> Math.round(value * 100.0D) + "%";
                case DECIBELS -> String.format(Locale.ROOT, "%+.1f dB", value);
                case SEMITONES -> String.format(Locale.ROOT, "%+.1f st", 12.0D * (Math.log(value) / Math.log(2.0D)));
                case HERTZ -> String.format(Locale.ROOT, "%.1f Hz", value);
                case BITS -> Math.round(value) + " bit";
                case PLAIN -> String.format(Locale.ROOT, "%.2f", value);
            };
        }
    }

    private final String id;
    private final Unit unit;
    private final double min;
    private final double max;
    private final double defaultValue;
    private final double neutralValue;
    private final boolean integral;

    VoiceParameter(String id, Unit unit, double min, double max, double defaultValue, double neutralValue) {
        this(id, unit, min, max, defaultValue, neutralValue, false);
    }

    VoiceParameter(String id, Unit unit, double min, double max, double defaultValue, double neutralValue, boolean integral) {
        this.id = id;
        this.unit = unit;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.neutralValue = neutralValue;
        this.integral = integral;
    }

    public String id() {
        return this.id;
    }

    public Unit unit() {
        return this.unit;
    }

    public double min() {
        return this.min;
    }

    public double max() {
        return this.max;
    }

    public double defaultValue() {
        return this.defaultValue;
    }

    public double neutralValue() {
        return this.neutralValue;
    }

    public boolean isIntegral() {
        return this.integral;
    }

    public double sanitize(double value) {
        if (!Double.isFinite(value)) {
            return this.defaultValue;
        }
        double clamped = Math.max(this.min, Math.min(this.max, value));
        return this.integral ? Math.rint(clamped) : clamped;
    }

    public String formatValue(double value) {
        return this.unit.format(value);
    }

    public String translationKey() {
        return "raufixvc.param." + this.id;
    }

    public String descriptionKey() {
        return translationKey() + ".desc";
    }
}
