package com.raufixbaba.voicechanger.client.model;

public record VoiceChangerState(
        boolean enabled,
        boolean selfListen,
        VoiceChangerPreset preset,
        int strength,
        String savedPresetName,
        String contributedVoiceId,
        VoiceProfile profile
) {
    public static VoiceChangerState defaults() {
        return new VoiceChangerState(
                false,
                false,
                VoiceChangerPreset.WOMAN,
                100,
                null,
                null,
                VoiceChangerPreset.WOMAN.profile()
        );
    }
}
