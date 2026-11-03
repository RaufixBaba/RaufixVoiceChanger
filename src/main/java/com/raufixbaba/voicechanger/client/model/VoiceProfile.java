package com.raufixbaba.voicechanger.client.model;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public final class VoiceProfile {
    private static final VoiceParameter[] PARAMETERS = VoiceParameter.values();

    private final double[] values;

    private VoiceProfile(double[] sanitizedValues) {
        this.values = sanitizedValues;
    }

    public static VoiceProfile defaults() {
        double[] values = new double[PARAMETERS.length];
        for (VoiceParameter parameter : PARAMETERS) {
            values[parameter.ordinal()] = parameter.defaultValue();
        }
        return new VoiceProfile(values);
    }

    public static VoiceProfile neutral() {
        double[] values = new double[PARAMETERS.length];
        for (VoiceParameter parameter : PARAMETERS) {
            values[parameter.ordinal()] = parameter.neutralValue();
        }
        values[VoiceParameter.MIX.ordinal()] = 1.0D;
        return new VoiceProfile(values);
    }

    public static Builder builder() {
        return new Builder(defaults());
    }

    public double get(VoiceParameter parameter) {
        return this.values[parameter.ordinal()];
    }

    public int getInt(VoiceParameter parameter) {
        return (int) Math.rint(this.values[parameter.ordinal()]);
    }

    public VoiceProfile with(VoiceParameter parameter, double value) {
        double sanitized = parameter.sanitize(value);
        if (sanitized == this.values[parameter.ordinal()]) {
            return this;
        }
        double[] copy = this.values.clone();
        copy[parameter.ordinal()] = sanitized;
        return new VoiceProfile(copy);
    }

    public VoiceProfile scaledBy(double amount) {
        double clamped = Math.max(0.0D, Math.min(1.0D, amount));
        if (clamped >= 1.0D) {
            return this;
        }
        double[] scaled = new double[PARAMETERS.length];
        for (VoiceParameter parameter : PARAMETERS) {
            double neutral = parameter.neutralValue();
            double target = this.values[parameter.ordinal()];
            scaled[parameter.ordinal()] = parameter.sanitize(neutral + (target - neutral) * clamped);
        }
        return new VoiceProfile(scaled);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof VoiceProfile profile && Arrays.equals(this.values, profile.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    public static final class Builder {
        private final Map<VoiceParameter, Double> overrides = new EnumMap<>(VoiceParameter.class);
        private final VoiceProfile base;

        private Builder(VoiceProfile base) {
            this.base = base;
        }

        public Builder set(VoiceParameter parameter, double value) {
            this.overrides.put(parameter, value);
            return this;
        }

        public VoiceProfile build() {
            double[] values = this.base.values.clone();
            this.overrides.forEach((parameter, value) -> values[parameter.ordinal()] = parameter.sanitize(value));
            return new VoiceProfile(values);
        }
    }
}
