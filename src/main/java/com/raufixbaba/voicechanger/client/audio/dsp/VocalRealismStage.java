package com.raufixbaba.voicechanger.client.audio.dsp;

import java.util.Arrays;

/**
 * Studio Vocal Realism processor introduced in Raufix Voice Changer V1.0 BETA.
 *
 * <ul>
 *   <li><b>Clarity:</b> Carves out 185 Hz male chest boominess and enhances
 *       2.75 kHz vocal-fold articulation so female/young voices sound genuinely
 *       human instead of muffled or chesty.</li>
 *   <li><b>Breathiness:</b> Envelope-tracked velvety glottal air (2.5–6.8 kHz)
 *       that rides strictly on active speech syllables and stays 100% silent at rest.</li>
 *   <li><b>Warmth:</b> Smooth optical vocal compression + subtle even-order tube
 *       saturation that glues pitch-shifted harmonics naturally for Opus voice chat.</li>
 *   <li><b>Chorus:</b> Dual-LFO micro-detune ensemble doubler for angelic,
 *       mystical, reaper, and sci-fi voices.</li>
 * </ul>
 */
public final class VocalRealismStage {
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double CHEST_MUD_HZ = 185.0D;
    private static final double CHEST_MUD_Q = 1.15D;
    private static final double PRESENCE_HZ = 2_750.0D;
    private static final double PRESENCE_Q = 1.05D;
    private static final double BREATH_HP_HZ = 2_500.0D;
    private static final double BREATH_LP_HZ = 6_800.0D;
    private static final double RAMP_SECONDS = 0.03D;

    private final double sampleRate;
    private final Biquad chestCutFilter = new Biquad();
    private final Biquad presenceFilter = new Biquad();
    private final Biquad breathHighPass = new Biquad();
    private final Biquad breathLowPass = new Biquad();

    private final SmoothedValue breathiness;
    private final SmoothedValue warmth;
    private final SmoothedValue chorus;

    private final float[] chorusBuffer;
    private int chorusWrite;
    private double lfoPhase1;
    private double lfoPhase2;

    private double speechEnvelope;
    private double compEnvelope;
    private final double attackCoeff;
    private final double releaseCoeff;
    private double dcPrevX;
    private double dcPrevY;
    private double lastClarity = -1.0D;
    private int rngState = 0x6D2B79F5;

    public VocalRealismStage(double sampleRate) {
        this.sampleRate = sampleRate;
        this.breathiness = new SmoothedValue(sampleRate, RAMP_SECONDS, 0.0D);
        this.warmth = new SmoothedValue(sampleRate, RAMP_SECONDS, 0.0D);
        this.chorus = new SmoothedValue(sampleRate, RAMP_SECONDS, 0.0D);

        this.breathHighPass.setHighPass(sampleRate, BREATH_HP_HZ, 0.707D);
        this.breathLowPass.setLowPass(sampleRate, BREATH_LP_HZ, 0.707D);

        int maxDelaySamples = Math.max(64, (int) Math.ceil(sampleRate * 0.030D));
        this.chorusBuffer = new float[maxDelaySamples];

        this.attackCoeff = 1.0D - Math.exp(-1.0D / (sampleRate * 0.008D));
        this.releaseCoeff = 1.0D - Math.exp(-1.0D / (sampleRate * 0.085D));

        configureClarity(0.0D);
    }

    public void setSettings(Settings settings) {
        this.breathiness.setTarget(settings.breathiness());
        this.warmth.setTarget(settings.warmth());
        this.chorus.setTarget(settings.chorus());
        configureClarity(settings.clarity());
    }

    private void configureClarity(double clarity) {
        double clamped = Math.max(0.0D, Math.min(1.0D, clarity));
        if (Math.abs(clamped - this.lastClarity) < 0.005D) {
            return;
        }
        this.lastClarity = clamped;
        this.chestCutFilter.setPeaking(this.sampleRate, CHEST_MUD_HZ, CHEST_MUD_Q, -7.5D * clamped);
        this.presenceFilter.setPeaking(this.sampleRate, PRESENCE_HZ, PRESENCE_Q, 4.2D * clamped);
    }

    public double process(double input) {
        double signal = input;
        if (this.lastClarity > 0.005D) {
            signal = this.presenceFilter.process(this.chestCutFilter.process(signal));
        }

        signal = applyBreathiness(signal);
        signal = applyWarmth(signal);
        signal = applyChorus(signal);
        return signal;
    }

    private double applyBreathiness(double input) {
        double amount = this.breathiness.next();
        double abs = Math.abs(input);
        double coeff = abs > this.speechEnvelope ? this.attackCoeff : this.releaseCoeff;
        this.speechEnvelope += coeff * (abs - this.speechEnvelope);

        if (amount < 0.002D || this.speechEnvelope < 0.005D) {
            return input;
        }

        // Fast Xorshift-style PRNG shaped into velvety vocal aspiration
        int x = this.rngState;
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        this.rngState = x;
        double white = ((x & 0xFFFF) / 32_767.5D) - 1.0D;
        double shapedAir = this.breathLowPass.process(this.breathHighPass.process(white));

        // Modulate air by speech envelope so breath rides organically on voiced formants
        double envGain = Math.min(0.45D, this.speechEnvelope * 1.35D);
        return input + shapedAir * envGain * amount * 0.22D;
    }

    private double applyWarmth(double input) {
        double amount = this.warmth.next();
        if (amount < 0.002D) {
            return input;
        }

        // Smooth optical vocal leveling
        double abs = Math.abs(input);
        double coeff = abs > this.compEnvelope ? this.attackCoeff : this.releaseCoeff;
        this.compEnvelope += coeff * (abs - this.compEnvelope);

        double compGain = 1.0D;
        double threshold = 0.16D;
        if (this.compEnvelope > threshold) {
            double ratio = 1.0D + amount * 1.6D;
            double targetEnv = threshold + (this.compEnvelope - threshold) / ratio;
            compGain = (targetEnv / Math.max(1.0e-6D, this.compEnvelope)) * (1.0D + amount * 0.14D);
        }
        double leveled = input * compGain;

        // Subtle even-order tube harmonic warmth + DC block
        double drive = 1.0D + amount * 0.85D;
        double shaped = Math.tanh(leveled * drive + 0.08D * amount * leveled * leveled) / Math.tanh(drive);
        double dcBlocked = shaped - this.dcPrevX + 0.995D * this.dcPrevY;
        this.dcPrevX = shaped;
        this.dcPrevY = dcBlocked;

        return input + (dcBlocked - input) * amount;
    }

    private double applyChorus(double input) {
        double amount = this.chorus.next();
        this.chorusBuffer[this.chorusWrite] = (float) input;

        if (amount < 0.002D) {
            this.chorusWrite = (this.chorusWrite + 1) % this.chorusBuffer.length;
            return input;
        }

        double delay1Ms = 11.0D + 2.6D * Math.sin(this.lfoPhase1);
        double delay2Ms = 16.5D + 3.1D * Math.cos(this.lfoPhase2);
        this.lfoPhase1 += TWO_PI * 0.85D / this.sampleRate;
        if (this.lfoPhase1 >= TWO_PI) {
            this.lfoPhase1 -= TWO_PI;
        }
        this.lfoPhase2 += TWO_PI * 1.17D / this.sampleRate;
        if (this.lfoPhase2 >= TWO_PI) {
            this.lfoPhase2 -= TWO_PI;
        }

        double tap1 = readFractionalDelay(delay1Ms * 0.001D * this.sampleRate);
        double tap2 = readFractionalDelay(delay2Ms * 0.001D * this.sampleRate);
        this.chorusWrite = (this.chorusWrite + 1) % this.chorusBuffer.length;

        double ensemble = 0.5D * (tap1 + tap2);
        return input * (1.0D - 0.35D * amount) + ensemble * (0.55D * amount);
    }

    private double readFractionalDelay(double delaySamples) {
        int len = this.chorusBuffer.length;
        double clamped = Math.max(1.0D, Math.min(len - 2.0D, delaySamples));
        double readPos = this.chorusWrite - clamped;
        while (readPos < 0.0D) {
            readPos += len;
        }
        int idx0 = ((int) readPos) % len;
        int idx1 = (idx0 + 1) % len;
        double frac = readPos - Math.floor(readPos);
        return this.chorusBuffer[idx0] + frac * (this.chorusBuffer[idx1] - this.chorusBuffer[idx0]);
    }

    public void reset() {
        this.chestCutFilter.reset();
        this.presenceFilter.reset();
        this.breathHighPass.reset();
        this.breathLowPass.reset();
        Arrays.fill(this.chorusBuffer, 0f);
        this.chorusWrite = 0;
        this.speechEnvelope = 0.0D;
        this.compEnvelope = 0.0D;
        this.dcPrevX = 0.0D;
        this.dcPrevY = 0.0D;
    }

    public record Settings(
            double breathiness,
            double warmth,
            double clarity,
            double chorus
    ) {
    }
}
