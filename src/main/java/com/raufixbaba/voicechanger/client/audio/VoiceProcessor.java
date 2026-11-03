package com.raufixbaba.voicechanger.client.audio;

import com.raufixbaba.voicechanger.client.audio.dsp.VocalPitchShifter;
import com.raufixbaba.voicechanger.client.model.VoiceProfile;

public final class VoiceProcessor {
    public static final double SAMPLE_RATE = 48_000.0D;

    private VoiceChannelChain[] chains = new VoiceChannelChain[0];
    private float[][] channelBuffers = new float[0][];

    private VoiceProfile cachedProfile;
    private int cachedStrength = -1;
    private VoiceSettings cachedSettings;

    public void process(short[] samples, int channels, VoiceProfile profile, int strength) {
        process(samples, samples == null ? 0 : samples.length, channels, profile, strength);
    }

    public void process(short[] samples, int length, int channels, VoiceProfile profile, int strength) {
        if (samples == null || length <= 0 || length > samples.length) {
            return;
        }

        int channelCount = Math.max(1, channels);
        int frames = length / channelCount;
        if (frames == 0) {
            return;
        }

        prepare(channelCount, frames);
        VoiceSettings settings = resolveSettings(profile, strength);

        deinterleave(samples, channelCount, frames);
        for (int channel = 0; channel < channelCount; channel++) {
            this.chains[channel].configure(settings);
            this.chains[channel].process(this.channelBuffers[channel], frames);
        }
        interleave(samples, channelCount, frames);
    }

    public void reset() {
        for (VoiceChannelChain chain : this.chains) {
            chain.reset();
        }
    }

    public double speakerPitchHz() {
        return this.chains.length > 0 ? this.chains[0].speakerPitchHz() : 0.0D;
    }

    public double appliedPitchRatio() {
        return this.chains.length > 0 ? this.chains[0].appliedPitchRatio() : 1.0D;
    }

    public float[] spectrumBands() {
        float[] bands = new float[VocalPitchShifter.VISUAL_BANDS];
        if (this.chains.length > 0) {
            this.chains[0].copySpectrumBands(bands);
        }
        return bands;
    }

    public double takePeakLevel() {
        double peak = 0.0D;
        for (VoiceChannelChain chain : this.chains) {
            peak = Math.max(peak, chain.takePeakLevel());
        }
        return peak;
    }

    private VoiceSettings resolveSettings(VoiceProfile profile, int strength) {
        int clampedStrength = Math.max(0, Math.min(100, strength));
        if (this.cachedSettings != null && clampedStrength == this.cachedStrength && profile.equals(this.cachedProfile)) {
            return this.cachedSettings;
        }

        this.cachedProfile = profile;
        this.cachedStrength = clampedStrength;
        this.cachedSettings = VoiceSettings.from(profile.scaledBy(clampedStrength / 100.0D));
        return this.cachedSettings;
    }

    private void prepare(int channelCount, int frames) {
        if (this.chains.length != channelCount) {
            VoiceChannelChain[] resized = new VoiceChannelChain[channelCount];
            for (int i = 0; i < channelCount; i++) {
                resized[i] = i < this.chains.length ? this.chains[i] : new VoiceChannelChain(SAMPLE_RATE);
            }
            this.chains = resized;
            this.channelBuffers = new float[channelCount][frames];
            return;
        }

        if (this.channelBuffers.length != channelCount || this.channelBuffers[0].length < frames) {
            this.channelBuffers = new float[channelCount][frames];
        }
    }

    private void deinterleave(short[] samples, int channelCount, int frames) {
        for (int frame = 0; frame < frames; frame++) {
            int base = frame * channelCount;
            for (int channel = 0; channel < channelCount; channel++) {
                this.channelBuffers[channel][frame] = samples[base + channel] / 32_768f;
            }
        }
    }

    private void interleave(short[] samples, int channelCount, int frames) {
        for (int frame = 0; frame < frames; frame++) {
            int base = frame * channelCount;
            for (int channel = 0; channel < channelCount; channel++) {
                float value = this.channelBuffers[channel][frame];
                if (!Float.isFinite(value)) {
                    value = 0f;
                }
                float clamped = Math.max(-1f, Math.min(1f, value));
                samples[base + channel] = (short) Math.round(clamped * 32_767f);
            }
        }
    }
}
