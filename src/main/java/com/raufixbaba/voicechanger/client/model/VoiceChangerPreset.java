package com.raufixbaba.voicechanger.client.model;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.raufixbaba.voicechanger.client.model.VoiceParameter.AUTOTUNE_KEY;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.AUTOTUNE_MIX;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.AUTOTUNE_SCALE;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.AUTOTUNE_SPEED;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.BIT_DEPTH;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.BREATHINESS;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.CHORUS;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.CLARITY;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.DE_ESS;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.DISTORTION;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.FORMANT;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.GAIN;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.GATE;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.GROWL;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.HIGH_EQ;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.LOW_EQ;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.MID_EQ;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.MIX;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.NOISE;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.PITCH;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.REVERB_DECAY;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.REVERB_MIX;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.REVERB_SIZE;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.ROBOT_FREQUENCY;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.ROBOT_MIX;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.TREMOLO_DEPTH;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.TREMOLO_RATE;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.VOICE_MATCH;
import static com.raufixbaba.voicechanger.client.model.VoiceParameter.WARMTH;

/**
 * 26 Studio-Tuned Built-In Voices for Raufix Voice Changer V1.0 BETA —
 * loudness-normalized for both desktop and mobile/Android microphones, plus {@link #CUSTOM}.
 */
public enum VoiceChangerPreset {
    CUSTOM("custom", VoiceCategory.SAVED, false, "✦") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.neutral();
        }
    },

    // =========================================================================
    // 1. REALISTIC HUMAN VOICES (Undetectable vocal-tract + breathiness + warmth)
    // =========================================================================

    /** Ultra-realistic young woman (~204 Hz target, chest-mud cut, velvety glottal air, smooth formants). */
    WOMAN("woman", VoiceCategory.REALISTIC, false, "♀") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.62D)
                    .set(PITCH, 1.62D).set(VOICE_MATCH, 0.82D).set(FORMANT, 1.18D)
                    .set(CLARITY, 0.54D).set(BREATHINESS, 0.24D).set(WARMTH, 0.56D)
                    .set(LOW_EQ, -1.6D).set(MID_EQ, 1.8D).set(HIGH_EQ, 2.2D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.62D)
                    .build();
        }
    },

    /** Sweet, soft, slightly breathy young girl / e-girl (~216 Hz target). */
    SOFT_GIRL("soft_girl", VoiceCategory.REALISTIC, true, "♡") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.72D)
                    .set(PITCH, 1.72D).set(VOICE_MATCH, 0.84D).set(FORMANT, 1.22D)
                    .set(CLARITY, 0.58D).set(BREATHINESS, 0.36D).set(WARMTH, 0.60D)
                    .set(LOW_EQ, -1.8D).set(MID_EQ, 2.0D).set(HIGH_EQ, 2.6D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.66D)
                    .build();
        }
    },

    /** Calm, natural, warm adult woman (~186 Hz target — super effortless & believable). */
    MATURE_WOMAN("mature_woman", VoiceCategory.REALISTIC, true, "♀") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.54D)
                    .set(PITCH, 1.50D).set(VOICE_MATCH, 0.80D).set(FORMANT, 1.15D)
                    .set(CLARITY, 0.44D).set(BREATHINESS, 0.16D).set(WARMTH, 0.58D)
                    .set(LOW_EQ, -1.4D).set(MID_EQ, 1.5D).set(HIGH_EQ, 1.6D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.55D)
                    .build();
        }
    },

    /** Charismatic deep male voice (~98 Hz baritone with warm studio presence). */
    MAN("man", VoiceCategory.REALISTIC, false, "♂") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.12D)
                    .set(PITCH, 0.83D).set(VOICE_MATCH, 0.72D).set(FORMANT, 0.88D)
                    .set(GROWL, 0.10D).set(WARMTH, 0.52D).set(CLARITY, 0.18D)
                    .set(LOW_EQ, 3.0D).set(MID_EQ, 0.6D).set(HIGH_EQ, -1.0D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.28D)
                    .build();
        }
    },

    /** NEW: Ultra-clean, resonant, authoritative GigaChad alpha baritone (~90 Hz). */
    GIGACHAD("gigachad", VoiceCategory.REALISTIC, true, "♛") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.16D)
                    .set(PITCH, 0.77D).set(VOICE_MATCH, 0.75D).set(FORMANT, 0.84D)
                    .set(GROWL, 0.16D).set(WARMTH, 0.72D).set(CLARITY, 0.28D)
                    .set(LOW_EQ, 4.2D).set(MID_EQ, 1.0D).set(HIGH_EQ, 0.8D)
                    .set(REVERB_MIX, 0.05D).set(REVERB_SIZE, 0.28D).set(REVERB_DECAY, 0.32D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.35D)
                    .build();
        }
    },

    /** Velvety radio host / cinema voiceover announcer. */
    DEEP_ANNOUNCER("deep_announcer", VoiceCategory.REALISTIC, true, "🎙") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.10D)
                    .set(PITCH, 0.88D).set(VOICE_MATCH, 0.68D).set(FORMANT, 0.91D)
                    .set(WARMTH, 0.78D).set(CLARITY, 0.35D).set(GROWL, 0.08D)
                    .set(LOW_EQ, 3.8D).set(MID_EQ, 1.2D).set(HIGH_EQ, 1.5D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.45D)
                    .build();
        }
    },

    /** Natural 16-18 yr young male voice (~144 Hz). */
    TEEN_BOY("teen_boy", VoiceCategory.REALISTIC, true, "♂") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.24D)
                    .set(PITCH, 1.20D).set(VOICE_MATCH, 0.75D).set(FORMANT, 1.08D)
                    .set(CLARITY, 0.32D).set(WARMTH, 0.42D)
                    .set(LOW_EQ, -1.8D).set(MID_EQ, 1.0D).set(HIGH_EQ, 1.2D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.42D)
                    .build();
        }
    },

    /** Natural child voice (no chipmunk artifacts). */
    KID("kid", VoiceCategory.REALISTIC, false, "★") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.38D)
                    .set(PITCH, 1.42D).set(VOICE_MATCH, 0.78D).set(FORMANT, 1.34D)
                    .set(CLARITY, 0.55D).set(BREATHINESS, 0.14D).set(WARMTH, 0.44D)
                    .set(LOW_EQ, -4.2D).set(MID_EQ, 1.8D).set(HIGH_EQ, 2.0D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.60D)
                    .build();
        }
    },

    /** Weathered, wise old man with subtle vocal-fold tremor & rasp. */
    OLD_MAN("old_man", VoiceCategory.REALISTIC, true, "⏳") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.20D)
                    .set(PITCH, 0.91D).set(VOICE_MATCH, 0.65D).set(FORMANT, 0.94D)
                    .set(BREATHINESS, 0.28D).set(WARMTH, 0.48D)
                    .set(TREMOLO_DEPTH, 0.14D).set(TREMOLO_RATE, 5.4D).set(DISTORTION, 0.05D)
                    .set(LOW_EQ, -1.0D).set(MID_EQ, 2.0D).set(HIGH_EQ, -2.0D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.30D)
                    .build();
        }
    },

    // =========================================================================
    // 2. ANIME, POP CULTURE & ROLEPLAY CHARACTERS
    // =========================================================================

    /** Bright, cute expressive anime heroine voice. */
    ANIME_GIRL("anime_girl", VoiceCategory.CHARACTER, true, "✿") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.68D)
                    .set(PITCH, 1.80D).set(VOICE_MATCH, 0.85D).set(FORMANT, 1.28D)
                    .set(CLARITY, 0.60D).set(BREATHINESS, 0.28D).set(WARMTH, 0.54D)
                    .set(LOW_EQ, -2.0D).set(MID_EQ, 2.4D).set(HIGH_EQ, 3.0D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.68D)
                    .build();
        }
    },

    /** Dark Knight gravelly vigilante cowl voice. */
    BATMAN("batman", VoiceCategory.CHARACTER, false, "🦇") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.12D)
                    .set(PITCH, 0.79D).set(VOICE_MATCH, 0.70D).set(FORMANT, 0.81D)
                    .set(GROWL, 0.48D).set(WARMTH, 0.62D).set(DISTORTION, 0.15D)
                    .set(LOW_EQ, 4.2D).set(MID_EQ, 1.8D).set(HIGH_EQ, -2.8D)
                    .set(REVERB_MIX, 0.08D).set(REVERB_SIZE, 0.24D).set(REVERB_DECAY, 0.36D)
                    .set(GATE, 0.12D).set(DE_ESS, 0.35D)
                    .build();
        }
    },

    /** Ghostface iconic horror phone-modulator voice. */
    GHOSTFACE("ghostface", VoiceCategory.CHARACTER, true, "🔪") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.24D)
                    .set(PITCH, 0.78D).set(VOICE_MATCH, 0.68D).set(FORMANT, 0.83D)
                    .set(GROWL, 0.26D).set(VoiceParameter.RADIO, 0.52D)
                    .set(WARMTH, 0.58D).set(DISTORTION, 0.14D)
                    .set(MID_EQ, 3.2D).set(HIGH_EQ, -1.2D)
                    .set(REVERB_MIX, 0.07D).set(REVERB_SIZE, 0.30D).set(REVERB_DECAY, 0.38D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.32D)
                    .build();
        }
    },

    /** Dark Sith Lord metallic respirator helmet authority. */
    DARTH_LORD("darth_lord", VoiceCategory.CHARACTER, true, "⚔") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.24D)
                    .set(PITCH, 0.75D).set(VOICE_MATCH, 0.72D).set(FORMANT, 0.78D)
                    .set(GROWL, 0.32D).set(CHORUS, 0.22D).set(WARMTH, 0.62D)
                    .set(ROBOT_MIX, 0.12D).set(ROBOT_FREQUENCY, 48.0D)
                    .set(LOW_EQ, 4.0D).set(MID_EQ, 1.5D).set(HIGH_EQ, -2.5D)
                    .set(REVERB_MIX, 0.12D).set(REVERB_SIZE, 0.35D).set(REVERB_DECAY, 0.42D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.28D)
                    .build();
        }
    },

    /** Ancient Mystic Mage / Fantasy RP sage with arcane ensemble aura. */
    MYSTIC_MAGE("mystic_mage", VoiceCategory.CHARACTER, true, "✧") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.18D)
                    .set(PITCH, 0.86D).set(VOICE_MATCH, 0.65D).set(FORMANT, 0.89D)
                    .set(CHORUS, 0.38D).set(BREATHINESS, 0.18D).set(WARMTH, 0.50D)
                    .set(LOW_EQ, 2.5D).set(MID_EQ, 0.6D).set(HIGH_EQ, 1.8D)
                    .set(REVERB_MIX, 0.22D).set(REVERB_SIZE, 0.74D).set(REVERB_DECAY, 0.66D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.32D)
                    .build();
        }
    },

    /** NEW: Tactical SWAT / Military Commander crisp walkie-talkie comms. */
    RADIO_COMMANDER("radio_commander", VoiceCategory.CHARACTER, true, "🎖") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.24D)
                    .set(PITCH, 0.90D).set(VOICE_MATCH, 0.70D).set(FORMANT, 0.92D)
                    .set(VoiceParameter.RADIO, 0.84D).set(WARMTH, 0.65D).set(CLARITY, 0.38D)
                    .set(GROWL, 0.10D).set(DISTORTION, 0.16D).set(NOISE, 0.04D)
                    .set(MID_EQ, 3.4D).set(HIGH_EQ, 0.8D)
                    .set(GATE, 0.12D).set(DE_ESS, 0.34D)
                    .build();
        }
    },

    // =========================================================================
    // 3. MONSTER, DARK HORROR & FANTASY
    // =========================================================================

    /** Colossal earth-shaking Titan. */
    TITAN("titan", VoiceCategory.FANTASY, false, "⛰") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.12D)
                    .set(PITCH, 0.70D).set(VOICE_MATCH, 0.68D).set(FORMANT, 0.65D)
                    .set(GROWL, 0.46D).set(WARMTH, 0.58D).set(DISTORTION, 0.06D)
                    .set(LOW_EQ, 5.2D).set(MID_EQ, -0.6D).set(HIGH_EQ, -3.2D)
                    .set(REVERB_MIX, 0.15D).set(REVERB_SIZE, 0.84D).set(REVERB_DECAY, 0.68D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.20D)
                    .build();
        }
    },

    /** Abyssal Demon with dark ensemble & cathedral space. */
    DEMON("demon", VoiceCategory.FANTASY, false, "🔥") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.34D)
                    .set(PITCH, 0.67D).set(VOICE_MATCH, 0.68D).set(FORMANT, 0.74D)
                    .set(GROWL, 0.54D).set(CHORUS, 0.30D).set(WARMTH, 0.52D).set(DISTORTION, 0.22D)
                    .set(LOW_EQ, 3.0D).set(MID_EQ, 1.0D).set(HIGH_EQ, -2.4D)
                    .set(REVERB_MIX, 0.17D).set(REVERB_SIZE, 0.65D).set(REVERB_DECAY, 0.66D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.22D)
                    .build();
        }
    },

    /** NEW: Cave Troll / Orc Warlord guttural cavern voice. */
    CAVE_TROLL("cave_troll", VoiceCategory.FANTASY, true, "👹") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.26D)
                    .set(PITCH, 0.72D).set(VOICE_MATCH, 0.68D).set(FORMANT, 0.68D)
                    .set(GROWL, 0.60D).set(WARMTH, 0.64D).set(DISTORTION, 0.18D)
                    .set(LOW_EQ, 4.5D).set(MID_EQ, 1.4D).set(HIGH_EQ, -3.0D)
                    .set(REVERB_MIX, 0.19D).set(REVERB_SIZE, 0.76D).set(REVERB_DECAY, 0.60D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.22D)
                    .build();
        }
    },

    /** Shadow Reaper — bone-chilling spectral wraith whisper-growl. */
    SHADOW_REAPER("shadow_reaper", VoiceCategory.FANTASY, true, "☠") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.32D)
                    .set(PITCH, 0.76D).set(VOICE_MATCH, 0.65D).set(FORMANT, 0.80D)
                    .set(BREATHINESS, 0.52D).set(CHORUS, 0.48D).set(GROWL, 0.28D).set(WARMTH, 0.45D)
                    .set(LOW_EQ, 1.5D).set(MID_EQ, 0.8D).set(HIGH_EQ, 2.5D)
                    .set(REVERB_MIX, 0.24D).set(REVERB_SIZE, 0.78D).set(REVERB_DECAY, 0.72D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.40D)
                    .build();
        }
    },

    /** Zombie / Undead Ghoul guttural horror rasp. */
    ZOMBIE_GHOUL("zombie_ghoul", VoiceCategory.FANTASY, true, "☣") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.32D)
                    .set(PITCH, 0.74D).set(VOICE_MATCH, 0.65D).set(FORMANT, 0.72D)
                    .set(GROWL, 0.42D).set(WARMTH, 0.46D).set(DISTORTION, 0.30D)
                    .set(TREMOLO_DEPTH, 0.18D).set(TREMOLO_RATE, 7.5D)
                    .set(LOW_EQ, 2.0D).set(MID_EQ, 2.2D).set(HIGH_EQ, -3.0D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.25D)
                    .build();
        }
    },

    /** Celestial Angelic voice with shimmering chorus & velvety air. */
    ANGELIC("angelic", VoiceCategory.FANTASY, true, "❖") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.65D)
                    .set(PITCH, 1.38D).set(VOICE_MATCH, 0.76D).set(FORMANT, 1.14D)
                    .set(CHORUS, 0.55D).set(BREATHINESS, 0.32D).set(WARMTH, 0.56D).set(CLARITY, 0.42D)
                    .set(LOW_EQ, -1.2D).set(MID_EQ, 1.5D).set(HIGH_EQ, 3.5D)
                    .set(REVERB_MIX, 0.24D).set(REVERB_SIZE, 0.82D).set(REVERB_DECAY, 0.74D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.58D)
                    .build();
        }
    },

    // =========================================================================
    // 4. SCI-FI, COMMS, FUNNY & STUDIO AUTOTUNE
    // =========================================================================

    /** Futuristic Cyborg / AI Android synth voice. */
    CYBORG("cyborg", VoiceCategory.EFFECTS, true, "⚙") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.72D)
                    .set(PITCH, 0.96D).set(FORMANT, 0.95D)
                    .set(ROBOT_MIX, 0.58D).set(ROBOT_FREQUENCY, 72.0D)
                    .set(CHORUS, 0.36D).set(WARMTH, 0.52D).set(BIT_DEPTH, 13.0D).set(CLARITY, 0.45D)
                    .set(MID_EQ, 2.4D).set(HIGH_EQ, 1.5D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.35D)
                    .build();
        }
    },

    /** Cosmic Alien extraterrestrial formant warble. */
    ALIEN("alien", VoiceCategory.EFFECTS, true, "👽") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.56D)
                    .set(PITCH, 1.26D).set(VOICE_MATCH, 0.65D).set(FORMANT, 0.74D)
                    .set(CHORUS, 0.52D).set(WARMTH, 0.48D).set(ROBOT_MIX, 0.22D).set(ROBOT_FREQUENCY, 38.0D)
                    .set(TREMOLO_DEPTH, 0.24D).set(TREMOLO_RATE, 6.2D)
                    .set(MID_EQ, 2.5D)
                    .set(GATE, 0.10D).set(DE_ESS, 0.40D)
                    .build();
        }
    },

    /** Police / Tactical Walkie-Talkie Radio. */
    RADIO("radio", VoiceCategory.EFFECTS, false, "📻") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.14D)
                    .set(PITCH, 1.00D).set(FORMANT, 1.00D)
                    .set(VoiceParameter.RADIO, 0.92D).set(WARMTH, 0.58D)
                    .set(MID_EQ, 3.0D)
                    .set(DISTORTION, 0.15D).set(NOISE, 0.05D).set(BIT_DEPTH, 12.0D)
                    .set(GATE, 0.12D).set(DE_ESS, 0.32D)
                    .build();
        }
    },

    /** NEW: Hilarious high-pitched Chipmunk / Helium cartoon voice. */
    CHIPMUNK("chipmunk", VoiceCategory.EFFECTS, true, "🐿") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.68D)
                    .set(PITCH, 1.92D).set(VOICE_MATCH, 0.55D).set(FORMANT, 1.56D)
                    .set(CLARITY, 0.55D).set(WARMTH, 0.56D)
                    .set(LOW_EQ, -1.8D).set(MID_EQ, 2.5D).set(HIGH_EQ, 2.5D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.64D)
                    .build();
        }
    },

    /** Hard-snapping Trap / Rapper Studio Autotune (C Minor). */
    TRAP_AUTOTUNE("trap_autotune", VoiceCategory.EFFECTS, true, "♪") {
        @Override
        VoiceProfile buildProfile() {
            return VoiceProfile.builder()
                    .set(MIX, 1.00D).set(GAIN, 1.20D)
                    .set(PITCH, 1.04D).set(FORMANT, 1.02D)
                    .set(AUTOTUNE_MIX, 1.00D).set(AUTOTUNE_SPEED, 0.96D)
                    .set(AUTOTUNE_KEY, 0.0D).set(AUTOTUNE_SCALE, 2.0D)
                    .set(WARMTH, 0.60D).set(CLARITY, 0.42D).set(CHORUS, 0.16D)
                    .set(HIGH_EQ, 2.2D)
                    .set(REVERB_MIX, 0.12D).set(REVERB_SIZE, 0.48D).set(REVERB_DECAY, 0.50D)
                    .set(GATE, 0.08D).set(DE_ESS, 0.48D)
                    .build();
        }
    };

    private static final List<VoiceChangerPreset> SELECTABLE = Arrays.stream(values())
            .filter(preset -> preset != CUSTOM)
            .toList();

    private final String key;
    private final VoiceCategory category;
    private final boolean v1New;
    private final String iconSymbol;
    private VoiceProfile profile;

    VoiceChangerPreset(String key, VoiceCategory category, boolean v1New, String iconSymbol) {
        this.key = key;
        this.category = category;
        this.v1New = v1New;
        this.iconSymbol = iconSymbol;
    }

    abstract VoiceProfile buildProfile();

    public synchronized VoiceProfile profile() {
        if (this.profile == null) {
            this.profile = buildProfile();
        }
        return this.profile;
    }

    public String getKey() {
        return this.key;
    }

    public VoiceCategory category() {
        return this.category;
    }

    public boolean isV1New() {
        return this.v1New;
    }

    public String iconSymbol() {
        return this.iconSymbol;
    }

    public String id() {
        return RaufixVoiceChanger.MOD_ID + ":" + this.key;
    }

    public String getTranslationKey() {
        return "raufixvc.preset." + this.key;
    }

    public String translationKey() {
        return getTranslationKey();
    }

    public String getDescriptionKey() {
        return "raufixvc.preset." + this.key + ".desc";
    }

    public String descriptionKey() {
        return getDescriptionKey();
    }

    public boolean isNewInV1() {
        return this.v1New;
    }

    public static List<VoiceChangerPreset> selectable() {
        return SELECTABLE;
    }

    public static List<VoiceChangerPreset> selectablePresets() {
        return SELECTABLE;
    }

    public static Optional<VoiceChangerPreset> byKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        for (VoiceChangerPreset preset : values()) {
            if (preset.key.equalsIgnoreCase(key) || preset.name().equalsIgnoreCase(key)) {
                return Optional.of(preset);
            }
        }
        return Optional.empty();
    }
}
