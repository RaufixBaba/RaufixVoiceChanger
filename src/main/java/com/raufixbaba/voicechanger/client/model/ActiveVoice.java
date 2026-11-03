package com.raufixbaba.voicechanger.client.model;

public record ActiveVoice(boolean active, VoiceProfile profile, int strength) {
    public static final ActiveVoice INACTIVE = new ActiveVoice(false, VoiceProfile.neutral(), 0);
}
