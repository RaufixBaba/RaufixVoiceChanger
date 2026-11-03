package com.raufixbaba.voicechanger.client.audio.dsp;

import java.util.Arrays;

/**
 * Next-Generation Realistic Vocal STFT Phase Vocoder + Cepstral Formant Shifter
 * built for Raufix Voice Changer V1.0 BETA.
 *
 * <p>Key acoustic upgrades over classic phase-vocoder shifters:</p>
 * <ol>
 *   <li><b>Zero-Hole Fractional Spectral Resampling:</b> Upward pitch shifts
 *       interpolate continuously across source excitation bins rather than
 *       rounding source indices, completely eliminating the ~30% missing-bin
 *       comb filter that made female/child voices sound metallic and fake.</li>
 *   <li><b>Voiced/Unvoiced Sibilant Protection:</b> Automatically detects
 *       unvoiced fricatives/sibilants (Turkish s, ş, ç, t, k, f, h) and
 *       preserves natural consonant texture above 3.6 kHz instead of pitching
 *       sibilants into unnatural whistles.</li>
 *   <li><b>Plosive Transient Phase Coherence:</b> Softly re-anchors phase
 *       accumulation on sharp consonant onsets so speech stays crisp.</li>
 *   <li><b>Temporally Smoothed Cepstral Envelope:</b> Glides vocal-tract
 *       formants smoothly across hops like real human articulators.</li>
 * </ol>
 */
public final class VocalPitchShifter {
    private static final double ENVELOPE_RESOLUTION_HZ = 380.0;
    private static final float MIN_ENVELOPE = 1.0e-6f;
    private static final float MAX_GAIN_CORRECTION = 3.5f;
    private static final float MIN_GAIN_CORRECTION = 0.30f;
    private static final float GAIN_CORRECTION_SMOOTHING = 0.25f;
    private static final float ENVELOPE_TEMPORAL_ALPHA = 0.65f;
    public static final int VISUAL_BANDS = 8;

    private final int fftSize;
    private final int stepSize;
    private final int half;
    private final int osamp;
    private final double expectedPhaseIncrement;
    private final float freqPerBin;
    private final float overlapAddScale;
    private final int lifterCutoff;
    private final int sibilantStartBin;
    private final int airStartBin;

    private final Radix2Fft fft;
    private final float[] analysisWindow;

    private final float[] spectrum;
    private final float[] analysisMagnitude;
    private final float[] analysisPhase;
    private final float[] analysisFrequency;
    private final float[] excitation;
    private final float[] envelope;
    private final float[] smoothedLogEnvelope;
    private final float[] shiftedMagnitude;
    private final float[] shiftedFrequency;
    private final float[] strongestContribution;
    private final float[] previousPhase;
    private final double[] summedPhase;
    private final float[] outputAccumulator;
    private final float[] cepstrum;
    private final float[] visualBands = new float[VISUAL_BANDS];

    private float pitchRatio = 1f;
    private float formantRatio = 1f;
    private float smoothedGainCorrection = 1f;
    private boolean envelopeInitialized;
    private double previousInputEnergy;

    public VocalPitchShifter(float sampleRate, int fftSize, int stepSize) {
        if (Integer.bitCount(fftSize) != 1) {
            throw new IllegalArgumentException("fftSize must be a power of two");
        }
        if (stepSize <= 0 || fftSize % stepSize != 0) {
            throw new IllegalArgumentException("fftSize must be an integer multiple of stepSize");
        }

        this.fftSize = fftSize;
        this.stepSize = stepSize;
        this.half = fftSize / 2;
        this.osamp = fftSize / stepSize;
        this.expectedPhaseIncrement = 2.0 * Math.PI * stepSize / fftSize;
        this.freqPerBin = sampleRate / fftSize;
        this.overlapAddScale = 8f / (3f * this.osamp);
        this.lifterCutoff = Math.max(4, Math.min(fftSize / 4,
                (int) Math.round(sampleRate / ENVELOPE_RESOLUTION_HZ)));
        this.sibilantStartBin = Math.min(this.half - 2, Math.max(8, Math.round(3_600f / this.freqPerBin)));
        this.airStartBin = Math.min(this.half - 1, Math.max(this.sibilantStartBin + 4, Math.round(7_200f / this.freqPerBin)));

        this.fft = new Radix2Fft(fftSize);
        this.analysisWindow = new float[fftSize];
        for (int i = 0; i < fftSize; i++) {
            this.analysisWindow[i] = (float) (0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / fftSize));
        }

        this.spectrum = new float[2 * fftSize];
        this.analysisMagnitude = new float[this.half];
        this.analysisPhase = new float[this.half];
        this.analysisFrequency = new float[this.half];
        this.excitation = new float[this.half];
        this.envelope = new float[this.half];
        this.smoothedLogEnvelope = new float[this.half];
        this.shiftedMagnitude = new float[this.half];
        this.shiftedFrequency = new float[this.half];
        this.strongestContribution = new float[this.half];
        this.previousPhase = new float[this.half];
        this.summedPhase = new double[this.half];
        this.outputAccumulator = new float[2 * fftSize];
        this.cepstrum = new float[2 * fftSize];
    }

    public int stepSize() {
        return this.stepSize;
    }

    public int lifterCutoff() {
        return this.lifterCutoff;
    }

    public int latencySamples() {
        return this.fftSize - this.stepSize;
    }

    public void setPitchRatio(float ratio) {
        this.pitchRatio = ratio;
    }

    public void setFormantRatio(float ratio) {
        this.formantRatio = ratio;
    }

    public boolean isTransparent() {
        return Math.abs(this.pitchRatio - 1f) < 0.002f && Math.abs(this.formantRatio - 1f) < 0.002f;
    }

    public void copyVisualBands(float[] dest) {
        int len = Math.min(dest.length, VISUAL_BANDS);
        System.arraycopy(this.visualBands, 0, dest, 0, len);
    }

    public void reset() {
        Arrays.fill(this.previousPhase, 0f);
        Arrays.fill(this.summedPhase, 0.0);
        Arrays.fill(this.outputAccumulator, 0f);
        Arrays.fill(this.smoothedLogEnvelope, 0f);
        Arrays.fill(this.visualBands, 0f);
        this.smoothedGainCorrection = 1f;
        this.envelopeInitialized = false;
        this.previousInputEnergy = 0.0;
    }

    public void process(float[] window, float[] outBlock) {
        if (window.length < this.fftSize) {
            throw new IllegalArgumentException("window must hold at least fftSize samples");
        }
        if (outBlock.length < this.stepSize) {
            throw new IllegalArgumentException("outBlock must hold at least stepSize samples");
        }

        analyse(window);

        double inputEnergy = computeEnergy(this.analysisMagnitude);
        boolean transientOnset = inputEnergy > 1.0e-5 && inputEnergy > this.previousInputEnergy * 4.5;
        this.previousInputEnergy = 0.70 * this.previousInputEnergy + 0.30 * inputEnergy;

        if (isTransparent()) {
            System.arraycopy(this.analysisMagnitude, 0, this.shiftedMagnitude, 0, this.half);
            System.arraycopy(this.analysisFrequency, 0, this.shiftedFrequency, 0, this.half);
        } else {
            float unvoicedFactor = estimateUnvoicedFactor();
            estimateSmoothedEnvelope();
            extractExcitation();
            shiftSpectrumFractional(unvoicedFactor);
            applyShiftedEnvelope(unvoicedFactor);
            matchEnergy(inputEnergy);
        }

        updateVisualBands(this.shiftedMagnitude);
        synthesise(transientOnset);
        overlapAdd(outBlock);
    }

    private void analyse(float[] window) {
        for (int i = 0; i < this.fftSize; i++) {
            this.spectrum[2 * i] = window[i] * this.analysisWindow[i];
            this.spectrum[2 * i + 1] = 0f;
        }
        this.fft.forward(this.spectrum);

        for (int i = 0; i < this.half; i++) {
            float real = this.spectrum[2 * i];
            float imaginary = this.spectrum[2 * i + 1];
            float magnitude = (float) (2.0 * Math.sqrt(real * real + imaginary * imaginary));
            float phase = (float) Math.atan2(imaginary, real);

            double phaseDelta = phase - this.previousPhase[i];
            this.previousPhase[i] = phase;
            this.analysisPhase[i] = phase;

            phaseDelta -= i * this.expectedPhaseIncrement;
            phaseDelta = wrapToPi(phaseDelta);

            double deviationInBins = this.osamp * phaseDelta / (2.0 * Math.PI);
            this.analysisMagnitude[i] = magnitude;
            this.analysisFrequency[i] = (float) ((i + deviationInBins) * this.freqPerBin);
        }
    }

    /**
     * Detects unvoiced fricatives/sibilants (s, ş, ç, f, t, h) by comparing
     * high-frequency energy (> 3.2 kHz) against voiced fundamental/formant energy.
     */
    private float estimateUnvoicedFactor() {
        int voicedEnd = Math.min(this.half, Math.max(4, Math.round(2_200f / this.freqPerBin)));
        double lowEnergy = 1.0e-9;
        for (int i = 2; i < voicedEnd; i++) {
            lowEnergy += this.analysisMagnitude[i] * this.analysisMagnitude[i];
        }
        double highEnergy = 0.0;
        for (int i = this.sibilantStartBin; i < this.half; i++) {
            highEnergy += this.analysisMagnitude[i] * this.analysisMagnitude[i];
        }
        double ratio = highEnergy / lowEnergy;
        if (ratio <= 0.45) {
            return 0f;
        }
        return (float) Math.min(1.0, (ratio - 0.45) / 1.8);
    }

    private void estimateSmoothedEnvelope() {
        for (int i = 0; i < this.half; i++) {
            float logMag = (float) Math.log(Math.max(MIN_ENVELOPE, this.analysisMagnitude[i]));
            this.cepstrum[2 * i] = logMag;
            this.cepstrum[2 * i + 1] = 0f;
        }
        for (int i = this.half; i < this.fftSize; i++) {
            int mirrored = this.fftSize - i;
            this.cepstrum[2 * i] = this.cepstrum[2 * Math.min(mirrored, this.half - 1)];
            this.cepstrum[2 * i + 1] = 0f;
        }

        this.fft.inverse(this.cepstrum);

        // Cosine taper at lifter cutoff prevents ringing ripples in the vocal tract curve
        int taperStart = Math.max(2, this.lifterCutoff - 4);
        for (int i = taperStart; i < this.lifterCutoff; i++) {
            float w = (float) (0.5 * (1.0 + Math.cos(Math.PI * (i - taperStart) / (this.lifterCutoff - taperStart))));
            this.cepstrum[2 * i] *= w;
            this.cepstrum[2 * i + 1] *= w;
            int mirror = this.fftSize - i;
            if (mirror < this.fftSize) {
                this.cepstrum[2 * mirror] *= w;
                this.cepstrum[2 * mirror + 1] *= w;
            }
        }
        for (int i = this.lifterCutoff; i <= this.fftSize - this.lifterCutoff; i++) {
            this.cepstrum[2 * i] = 0f;
            this.cepstrum[2 * i + 1] = 0f;
        }

        this.fft.forward(this.cepstrum);

        if (!this.envelopeInitialized) {
            for (int i = 0; i < this.half; i++) {
                this.smoothedLogEnvelope[i] = this.cepstrum[2 * i];
                this.envelope[i] = (float) Math.exp(this.smoothedLogEnvelope[i]);
            }
            this.envelopeInitialized = true;
        } else {
            for (int i = 0; i < this.half; i++) {
                this.smoothedLogEnvelope[i] = ENVELOPE_TEMPORAL_ALPHA * this.cepstrum[2 * i]
                        + (1f - ENVELOPE_TEMPORAL_ALPHA) * this.smoothedLogEnvelope[i];
                this.envelope[i] = (float) Math.exp(this.smoothedLogEnvelope[i]);
            }
        }
    }

    private void extractExcitation() {
        for (int i = 0; i < this.half; i++) {
            float env = this.envelope[i];
            this.excitation[i] = env > MIN_ENVELOPE ? this.analysisMagnitude[i] / env : 0f;
        }
    }

    /**
     * Shifts harmonic excitation without leaving spectral holes on upward shifts,
     * while protecting high-frequency sibilants and breathiness from unnatural
     * chipmunk whistle transposition.
     */
    private void shiftSpectrumFractional(float unvoicedFactor) {
        Arrays.fill(this.shiftedMagnitude, 0f);
        Arrays.fill(this.shiftedFrequency, 0f);

        if (this.pitchRatio >= 1.0f) {
            // Destination-driven continuous resampling: ZERO empty bins when pitching UP!
            float invRatio = 1.0f / this.pitchRatio;
            for (int k = 1; k < this.half; k++) {
                float src = k * invRatio;
                if (src >= this.half - 1) {
                    break;
                }
                int lo = (int) src;
                float frac = src - lo;
                int hi = lo + 1;

                float shiftedExc = this.excitation[lo] + frac * (this.excitation[hi] - this.excitation[lo]);
                int dom = this.analysisMagnitude[hi] > this.analysisMagnitude[lo] ? hi : lo;
                float shiftedFreq = this.analysisFrequency[dom] * this.pitchRatio;

                // Protect high-band sibilants & upper air when pitching up
                if (k >= this.sibilantStartBin) {
                    float bandProgress = Math.min(1f, (k - this.sibilantStartBin)
                            / (float) Math.max(1, this.airStartBin - this.sibilantStartBin));
                    float naturalKeep = Math.max(bandProgress * 0.65f, unvoicedFactor * 0.78f);
                    shiftedExc = shiftedExc * (1f - naturalKeep) + this.excitation[k] * naturalKeep;
                    shiftedFreq = shiftedFreq * (1f - naturalKeep) + this.analysisFrequency[k] * naturalKeep;
                }

                this.shiftedMagnitude[k] = shiftedExc;
                this.shiftedFrequency[k] = shiftedFreq;
            }
        } else {
            // Downward shift: distribute fractional source bin across [lo, hi] with dominant frequency tracking
            Arrays.fill(this.strongestContribution, 0f);
            for (int i = 1; i < this.half; i++) {
                float dst = i * this.pitchRatio;
                int lo = (int) dst;
                float frac = dst - lo;
                float exc = this.excitation[i];
                float contrib = this.analysisMagnitude[i];
                float targetFreq = this.analysisFrequency[i] * this.pitchRatio;

                if (lo >= 0 && lo < this.half) {
                    this.shiftedMagnitude[lo] += exc * (1f - frac);
                    if (contrib * (1f - frac) >= this.strongestContribution[lo]) {
                        this.strongestContribution[lo] = contrib * (1f - frac);
                        this.shiftedFrequency[lo] = targetFreq;
                    }
                }
                int hi = lo + 1;
                if (hi >= 0 && hi < this.half) {
                    this.shiftedMagnitude[hi] += exc * frac;
                    if (contrib * frac >= this.strongestContribution[hi]) {
                        this.strongestContribution[hi] = contrib * frac;
                        this.shiftedFrequency[hi] = targetFreq;
                    }
                }
            }
        }
    }

    private void applyShiftedEnvelope(float unvoicedFactor) {
        // Slightly gentler formant stretch during sharp sibilants keeps 's' and 'ş' natural
        float effectiveFormant = 1f + (this.formantRatio - 1f) * (1f - 0.30f * unvoicedFactor);
        for (int i = 0; i < this.half; i++) {
            this.shiftedMagnitude[i] *= sampleEnvelope(i / effectiveFormant);
        }
    }

    private float sampleEnvelope(float position) {
        if (position <= 0f) {
            return this.envelope[0];
        }
        if (position >= this.half - 1) {
            return this.envelope[this.half - 1];
        }
        int lower = (int) position;
        float fraction = position - lower;
        return this.envelope[lower] + (this.envelope[lower + 1] - this.envelope[lower]) * fraction;
    }

    private void matchEnergy(double inputEnergy) {
        double outputEnergy = computeEnergy(this.shiftedMagnitude);
        if (outputEnergy > 1.0e-12 && inputEnergy > 1.0e-12) {
            float instantGain = (float) Math.sqrt(inputEnergy / outputEnergy);
            instantGain = Math.max(MIN_GAIN_CORRECTION, Math.min(MAX_GAIN_CORRECTION, instantGain));
            this.smoothedGainCorrection += GAIN_CORRECTION_SMOOTHING * (instantGain - this.smoothedGainCorrection);
        }
        for (int i = 0; i < this.half; i++) {
            this.shiftedMagnitude[i] *= this.smoothedGainCorrection;
        }
    }

    private void synthesise(boolean transientOnset) {
        for (int i = 0; i < this.half; i++) {
            this.summedPhase[i] += phaseAdvanceFor(i);
            if (transientOnset) {
                double diff = wrapToPi(this.analysisPhase[i] - this.summedPhase[i]);
                this.summedPhase[i] += 0.35 * diff;
            }

            double magnitude = this.shiftedMagnitude[i];
            double phase = this.summedPhase[i];
            this.spectrum[2 * i] = (float) (magnitude * Math.cos(phase));
            this.spectrum[2 * i + 1] = (float) (magnitude * Math.sin(phase));
        }

        for (int i = this.half; i < this.fftSize; i++) {
            this.spectrum[2 * i] = 0f;
            this.spectrum[2 * i + 1] = 0f;
        }
    }

    private double phaseAdvanceFor(int bin) {
        double deviationInBins = this.shiftedFrequency[bin] / this.freqPerBin - bin;
        return 2.0 * Math.PI * deviationInBins / this.osamp + bin * this.expectedPhaseIncrement;
    }

    private void overlapAdd(float[] outBlock) {
        this.fft.inverse(this.spectrum);

        for (int i = 0; i < this.fftSize; i++) {
            this.outputAccumulator[i] += this.analysisWindow[i] * this.spectrum[2 * i] * this.overlapAddScale;
        }

        System.arraycopy(this.outputAccumulator, 0, outBlock, 0, this.stepSize);
        System.arraycopy(this.outputAccumulator, this.stepSize, this.outputAccumulator, 0, this.fftSize);
        Arrays.fill(this.outputAccumulator, this.fftSize, this.fftSize + this.stepSize, 0f);
    }

    private void updateVisualBands(float[] magnitudes) {
        int[] edges = {1, 3, 7, 15, 30, 60, 120, 240, Math.min(this.half, 420)};
        for (int b = 0; b < VISUAL_BANDS; b++) {
            int start = Math.min(this.half - 1, edges[b]);
            int end = Math.min(this.half, Math.max(start + 1, edges[b + 1]));
            double sum = 0.0;
            for (int i = start; i < end; i++) {
                sum += magnitudes[i];
            }
            float avg = (float) (sum / (end - start));
            float level = (float) Math.min(1.0, Math.sqrt(avg * 18.0));
            this.visualBands[b] = level > this.visualBands[b]
                    ? level
                    : this.visualBands[b] * 0.82f + level * 0.18f;
        }
    }

    private double computeEnergy(float[] magnitudes) {
        double energy = 0.0;
        for (int i = 0; i < this.half; i++) {
            energy += magnitudes[i] * magnitudes[i];
        }
        return energy;
    }

    private static double wrapToPi(double phase) {
        long quadrants = (long) (phase / Math.PI);
        quadrants += quadrants >= 0 ? (quadrants & 1L) : -(quadrants & 1L);
        return phase - Math.PI * quadrants;
    }
}
