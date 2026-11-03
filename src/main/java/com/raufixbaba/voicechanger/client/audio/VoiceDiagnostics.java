package com.raufixbaba.voicechanger.client.audio;

import com.raufixbaba.voicechanger.client.audio.dsp.VocalPitchShifter;

public record VoiceDiagnostics(
        double speakerPitchHz,
        double appliedPitchRatio,
        int blockFrames,
        int channels,
        int blocksPerSecond,
        float[] spectrumBands
) {
    private static final float[] EMPTY_BANDS = new float[VocalPitchShifter.VISUAL_BANDS];

    public static final VoiceDiagnostics IDLE = new VoiceDiagnostics(0.0D, 1.0D, 0, 0, 0, EMPTY_BANDS);

    public boolean isRunning() {
        return this.blocksPerSecond > 0;
    }

    public double targetPitchHz() {
        return this.speakerPitchHz * this.appliedPitchRatio;
    }

    public float band(int index) {
        if (this.spectrumBands == null || index < 0 || index >= this.spectrumBands.length) {
            return 0f;
        }
        return this.spectrumBands[index];
    }
}
