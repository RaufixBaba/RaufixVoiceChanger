package com.raufixbaba.voicechanger.client.audio;

import com.raufixbaba.voicechanger.client.audio.dsp.Biquad;
import com.raufixbaba.voicechanger.client.audio.dsp.DeEsser;
import com.raufixbaba.voicechanger.client.audio.dsp.NoiseGate;
import com.raufixbaba.voicechanger.client.audio.dsp.RoomReverb;
import com.raufixbaba.voicechanger.client.audio.dsp.SmoothedValue;
import com.raufixbaba.voicechanger.client.audio.dsp.SoftLimiter;
import com.raufixbaba.voicechanger.client.audio.dsp.SpeakerPitchTracker;
import com.raufixbaba.voicechanger.client.audio.dsp.ToneStack;
import com.raufixbaba.voicechanger.client.audio.dsp.VocalPitchShifter;
import com.raufixbaba.voicechanger.client.audio.dsp.VocalRealismStage;
import com.raufixbaba.voicechanger.client.model.AutotuneScale;
import java.util.Arrays;

/**
 * Complete single-channel DSP voice transformation pipeline for Raufix Voice Changer V1.0 BETA.
 */
public final class VoiceChannelChain {
    static final int FFT_SIZE = 1_024;
    static final int HOP_SIZE = 128;
    static final int LATENCY_SAMPLES = FFT_SIZE - 1;

    private static final double RUMBLE_HZ = 75.0D;
    private static final double REFERENCE_SPEAKER_HZ = 120.0D;
    private static final double MIN_CALIBRATION = 0.45D;
    private static final double MAX_CALIBRATION = 2.20D;
    private static final int SPEAKER_OBSERVE_HOPS = 16;
    private static final double FORMANT_CALIBRATION_EXPONENT = 0.28D;
    private static final double PARAMETER_RAMP_SECONDS = 0.03D;
    private static final int OUTPUT_QUEUE_SIZE = FFT_SIZE * 2;
    private static final double SHIFTER_FADE_SECONDS = 0.02D;
    private static final double SHIFT_THRESHOLD = 0.002D;

    private final NoiseGate gate;
    private final Biquad rumbleFilter = new Biquad();
    private final VocalPitchShifter shifter;
    private final AutotuneTracker autotune;
    private final SpeakerPitchTracker speakerPitch;
    private final DeEsser deEsser;
    private final VocalRealismStage realism;
    private final ToneStack tone;
    private final CharacterStage character;
    private final RoomReverb reverb;
    private final SoftLimiter limiter;

    private final SmoothedValue blend;
    private final SmoothedValue outputGain;
    private final SmoothedValue reverbMix;
    private final SmoothedValue shifterMix;

    private final float[] inputRing = new float[FFT_SIZE];
    private final float[] analysisWindow = new float[FFT_SIZE];
    private final float[] hopOutput = new float[HOP_SIZE];
    private final float[] outputQueue = new float[OUTPUT_QUEUE_SIZE];
    private final float[] dryDelay = new float[LATENCY_SAMPLES];

    private int inputWriteIndex;
    private long samplesSeen;
    private int samplesSinceHop;
    private int queueReadIndex;
    private int queueWriteIndex;
    private int queuedSamples;
    private int dryDelayIndex;

    private double pitchRatio = 1.0D;
    private double formantRatio = 1.0D;
    private AutotuneTracker.Settings autotuneSettings =
            new AutotuneTracker.Settings(0.0D, 0.6D, 0, AutotuneScale.CHROMATIC);
    private double peakLevel;
    private boolean reverbRunning;
    private double voiceMatch;
    private long hopCounter;
    private boolean shifterRunning;
    private double appliedPitchRatio = 1.0D;

    public VoiceChannelChain(double sampleRate) {
        this.gate = new NoiseGate(sampleRate);
        this.shifter = new VocalPitchShifter((float) sampleRate, FFT_SIZE, HOP_SIZE);
        this.autotune = new AutotuneTracker((float) sampleRate, FFT_SIZE, HOP_SIZE);
        this.speakerPitch = new SpeakerPitchTracker((float) sampleRate);
        this.deEsser = new DeEsser(sampleRate);
        this.realism = new VocalRealismStage(sampleRate);
        this.tone = new ToneStack(sampleRate);
        this.character = new CharacterStage(sampleRate);
        this.reverb = new RoomReverb(sampleRate);
        this.limiter = new SoftLimiter(sampleRate);

        this.blend = new SmoothedValue(sampleRate, PARAMETER_RAMP_SECONDS, 0.0D);
        this.outputGain = new SmoothedValue(sampleRate, PARAMETER_RAMP_SECONDS, 1.0D);
        this.reverbMix = new SmoothedValue(sampleRate, PARAMETER_RAMP_SECONDS, 0.0D);
        this.shifterMix = new SmoothedValue(sampleRate, SHIFTER_FADE_SECONDS, 0.0D);

        this.rumbleFilter.setHighPass(sampleRate, RUMBLE_HZ, 0.707D);
    }

    public void configure(VoiceSettings settings) {
        this.gate.setAmount(settings.gate());
        this.deEsser.setAmount(settings.deEss());
        this.realism.setSettings(settings.realism());
        this.tone.setEqualiser(settings.lowEqDb(), settings.midEqDb(), settings.highEqDb());
        this.tone.setRadioAmount(settings.radio());
        this.character.setSettings(settings.character());
        this.reverb.setRoom(settings.reverbSize(), settings.reverbDecay());

        this.blend.setTarget(settings.blend());
        this.outputGain.setTarget(settings.gain());
        this.reverbMix.setTarget(settings.reverbMix());

        this.pitchRatio = settings.pitch();
        this.voiceMatch = settings.voiceMatch();
        this.formantRatio = settings.formant();
        this.autotuneSettings = settings.autotune();
    }

    public void process(float[] block, int length) {
        for (int i = 0; i < length; i++) {
            block[i] = (float) processSample(block[i]);
        }
    }

    public double speakerPitchHz() {
        return this.speakerPitch.speakingPitchHz();
    }

    public double appliedPitchRatio() {
        return this.appliedPitchRatio;
    }

    public void copySpectrumBands(float[] dest) {
        this.shifter.copyVisualBands(dest);
    }

    public double takePeakLevel() {
        double peak = this.peakLevel;
        this.peakLevel = 0.0D;
        return peak;
    }

    public void reset() {
        this.gate.reset();
        this.rumbleFilter.reset();
        this.shifter.reset();
        this.autotune.reset();
        this.speakerPitch.reset();
        this.deEsser.reset();
        this.realism.reset();
        this.tone.reset();
        this.character.reset();
        this.reverb.reset();
        this.limiter.reset();

        Arrays.fill(this.inputRing, 0f);
        Arrays.fill(this.outputQueue, 0f);
        Arrays.fill(this.dryDelay, 0f);
        this.inputWriteIndex = 0;
        this.samplesSeen = 0L;
        this.samplesSinceHop = 0;
        this.queueReadIndex = 0;
        this.queueWriteIndex = 0;
        this.queuedSamples = 0;
        this.dryDelayIndex = 0;
        this.peakLevel = 0.0D;
        this.reverbRunning = false;
        this.shifterRunning = false;
        this.shifterMix.reset(0.0D);
        this.hopCounter = 0L;
    }

    private double processSample(double input) {
        if (!Double.isFinite(input)) {
            return 0.0D;
        }
        double magnitude = Math.abs(input);
        if (magnitude > this.peakLevel) {
            this.peakLevel = magnitude;
        }

        double cleaned = this.rumbleFilter.process(this.gate.process(input));
        this.speakerPitch.push((float) cleaned);

        double dry = pushDry(cleaned);
        double wet = advanceShifter(cleaned, dry);

        wet = this.deEsser.process(wet);
        wet = this.realism.process(wet);
        wet = this.tone.process(wet);
        wet = this.character.process(wet);

        wet = applyReverb(wet);
        wet *= this.outputGain.next();
        if (!Double.isFinite(wet)) {
            reset();
            wet = dry;
        }

        double blended = dry + (wet - dry) * this.blend.next();
        double limited = this.limiter.process(blended);
        return Double.isFinite(limited) ? Math.max(-1.0D, Math.min(1.0D, limited)) : 0.0D;
    }

    private double applyReverb(double input) {
        double amount = this.reverbMix.next();
        if (amount <= 0.001D) {
            if (this.reverbRunning) {
                this.reverb.reset();
                this.reverbRunning = false;
            }
            return input;
        }
        this.reverbRunning = true;
        double tail = this.reverb.process(input);
        return input * (1.0D - amount) + tail * amount;
    }

    private double pushDry(double sample) {
        double delayed = this.dryDelay[this.dryDelayIndex];
        this.dryDelay[this.dryDelayIndex] = (float) sample;
        this.dryDelayIndex = this.dryDelayIndex + 1 >= this.dryDelay.length ? 0 : this.dryDelayIndex + 1;
        return delayed;
    }

    private double advanceShifter(double input, double delayedDry) {
        this.inputRing[this.inputWriteIndex] = (float) input;
        this.inputWriteIndex = this.inputWriteIndex + 1 >= FFT_SIZE ? 0 : this.inputWriteIndex + 1;
        this.samplesSeen++;

        this.shifterMix.setTarget(isShiftRequired() ? 1.0D : 0.0D);
        double wetAmount = this.shifterMix.next();

        if (wetAmount <= 0.0001D) {
            stopShifter();
            return delayedDry;
        }

        this.shifterRunning = true;
        if (this.samplesSeen == FFT_SIZE) {
            runHop();
            this.samplesSinceHop = 0;
        } else if (this.samplesSeen > FFT_SIZE && ++this.samplesSinceHop >= HOP_SIZE) {
            this.samplesSinceHop = 0;
            runHop();
        }

        if (this.queuedSamples == 0) {
            return delayedDry;
        }

        float wet = this.outputQueue[this.queueReadIndex];
        this.queueReadIndex = this.queueReadIndex + 1 >= OUTPUT_QUEUE_SIZE ? 0 : this.queueReadIndex + 1;
        this.queuedSamples--;

        return delayedDry + (wet - delayedDry) * wetAmount;
    }

    private boolean isShiftRequired() {
        return Math.abs(this.pitchRatio - 1.0D) > SHIFT_THRESHOLD
                || Math.abs(this.formantRatio - 1.0D) > SHIFT_THRESHOLD
                || this.voiceMatch > 0.005D
                || this.autotuneSettings.amount() > 0.005D;
    }

    private void stopShifter() {
        if (!this.shifterRunning) {
            return;
        }
        this.shifter.reset();
        this.autotune.reset();
        this.queueReadIndex = 0;
        this.queueWriteIndex = 0;
        this.queuedSamples = 0;
        this.samplesSinceHop = 0;
        this.appliedPitchRatio = 1.0D;
        this.shifterRunning = false;
    }

    private void runHop() {
        flattenInputRing();

        if (this.voiceMatch > 0.005D && this.hopCounter % SPEAKER_OBSERVE_HOPS == 0) {
            this.speakerPitch.analyse();
        }
        this.hopCounter++;

        double correction = this.autotune.nextRatio(this.analysisWindow, this.autotuneSettings);
        double calibration = speakerCalibration();
        this.appliedPitchRatio = this.pitchRatio * calibration * correction;
        this.shifter.setPitchRatio((float) this.appliedPitchRatio);
        this.shifter.setFormantRatio((float) (this.formantRatio * formantCalibration(calibration)));
        this.shifter.process(this.analysisWindow, this.hopOutput);

        for (int i = 0; i < HOP_SIZE; i++) {
            this.outputQueue[this.queueWriteIndex] = this.hopOutput[i];
            this.queueWriteIndex = this.queueWriteIndex + 1 >= OUTPUT_QUEUE_SIZE ? 0 : this.queueWriteIndex + 1;
        }
        this.queuedSamples = Math.min(OUTPUT_QUEUE_SIZE, this.queuedSamples + HOP_SIZE);
    }

    private double speakerCalibration() {
        if (this.voiceMatch <= 0.005D) {
            return 1.0D;
        }
        double speakerHz = this.speakerPitch.speakingPitchHz();
        if (speakerHz <= 0.0D) {
            return 1.0D;
        }
        double calibration = Math.max(MIN_CALIBRATION,
                Math.min(MAX_CALIBRATION, REFERENCE_SPEAKER_HZ / speakerHz));
        return 1.0D + (calibration - 1.0D) * this.voiceMatch;
    }

    private static double formantCalibration(double calibration) {
        if (Math.abs(calibration - 1.0D) < 1.0e-6D) {
            return 1.0D;
        }
        return Math.pow(calibration, FORMANT_CALIBRATION_EXPONENT);
    }

    private void flattenInputRing() {
        int firstChunk = FFT_SIZE - this.inputWriteIndex;
        System.arraycopy(this.inputRing, this.inputWriteIndex, this.analysisWindow, 0, firstChunk);
        if (firstChunk < FFT_SIZE) {
            System.arraycopy(this.inputRing, 0, this.analysisWindow, firstChunk, FFT_SIZE - firstChunk);
        }
    }
}
