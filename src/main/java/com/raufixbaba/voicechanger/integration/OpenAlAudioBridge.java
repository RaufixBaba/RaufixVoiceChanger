package com.raufixbaba.voicechanger.integration;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.compat.ClientScreens;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.MicThread;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.speaker.Speaker;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerException;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerManager;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Native OpenAL audio bridge integrated directly with Simple Voice Chat's own
 * {@link SoundManager}, {@link SpeakerManager}, and {@link MicThread}.
 *
 * <p>Replaces all {@code javax.sound.sampled} device access so self-listen,
 * AI voice previews, and microphone capture never contend with OpenALSoft on
 * Android (MojoLauncher / PojavLauncher), Linux ALSA/PipeWire, macOS, or Windows.</p>
 */
public final class OpenAlAudioBridge {
    public static final int SAMPLE_RATE = 48_000;
    public static final int FRAME_SAMPLES = 960;

    private static volatile OfflineMicSession activeOfflineMic;

    private OpenAlAudioBridge() {
    }

    /**
     * Snapshot of Simple Voice Chat's live client state for studio diagnostics
     * and 1-click quick fixes.
     */
    public record SvcStatus(
            boolean clientPresent,
            boolean connected,
            boolean micThreadAlive,
            boolean muted,
            boolean forceMuted,
            boolean disabled,
            boolean forceDisabled,
            boolean voiceActivation
    ) {
        public boolean canTransmitNow() {
            return connected && micThreadAlive && !muted && !disabled;
        }
    }

    public static SvcStatus snapshotStatus() {
        try {
            ClientVoicechat client = ClientManager.getClient();
            ClientPlayerStateManager state = ClientManager.getPlayerStateManager();
            boolean clientPresent = client != null;
            boolean connected = client != null && client.getConnection() != null && !state.isDisconnected();
            MicThread mic = client != null ? client.getMicThread() : null;
            boolean micAlive = mic != null && !mic.isClosed();
            boolean muted = state.isMuted();
            boolean forceMuted = state.isForceMuted();
            boolean disabled = state.isDisabled();
            boolean forceDisabled = state.isForceDisabled();
            boolean vad = VoicechatClient.CLIENT_CONFIG != null
                    && MicrophoneActivationType.VOICE.equals(
                            VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get());
            return new SvcStatus(clientPresent, connected, micAlive, muted, forceMuted, disabled, forceDisabled, vad);
        } catch (Throwable t) {
            return new SvcStatus(false, false, false, false, false, false, false, false);
        }
    }

    /**
     * True when Simple Voice Chat's live server {@link MicThread} is actively
     * polling frames every 20 ms (meaning {@code MicThreadMixin} will deliver
     * untransmitted frames without opening or locking any microphone).
     */
    public static boolean hasActiveUpstreamMicPoll() {
        try {
            ClientVoicechat client = ClientManager.getClient();
            if (client == null) {
                return false;
            }
            MicThread mic = client.getMicThread();
            if (mic == null || mic.isClosed()) {
                return false;
            }
            return !ClientManager.getPlayerStateManager().isDisabled();
        } catch (Throwable t) {
            return false;
        }
    }

    /** Toggles Simple Voice Chat's microphone mute state if not force-muted by server. */
    public static boolean toggleMute() {
        try {
            ClientPlayerStateManager state = ClientManager.getPlayerStateManager();
            if (state.isForceMuted()) {
                return state.isMuted();
            }
            boolean next = !state.isMuted();
            state.setMuted(next);
            return next;
        } catch (Throwable t) {
            RaufixVoiceChanger.LOGGER.warn("Could not toggle Simple Voice Chat mute state", t);
            return false;
        }
    }

    /** Un-disables Simple Voice Chat if the player accidentally disabled it. */
    public static boolean setVoiceChatEnabled(boolean enabled) {
        try {
            ClientPlayerStateManager state = ClientManager.getPlayerStateManager();
            if (enabled && (!state.canEnable() || state.isForceDisabled())) {
                return !state.isDisabled();
            }
            state.setDisabled(!enabled);
            return !state.isDisabled();
        } catch (Throwable t) {
            RaufixVoiceChanger.LOGGER.warn("Could not change Simple Voice Chat enabled state", t);
            return false;
        }
    }

    /**
     * Toggles between Push-To-Talk ({@code PTT}) and Voice Activation ({@code VOICE}).
     * Especially helpful on Android / MojoLauncher where holding a keyboard PTT key is inconvenient.
     *
     * @return {@code true} if Voice Activation ({@code VOICE}) is now selected
     */
    public static boolean toggleActivationMode() {
        try {
            if (VoicechatClient.CLIENT_CONFIG == null) {
                return false;
            }
            MicrophoneActivationType current = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
            MicrophoneActivationType next = MicrophoneActivationType.VOICE.equals(current)
                    ? MicrophoneActivationType.PTT
                    : MicrophoneActivationType.VOICE;
            VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(next).save();
            return MicrophoneActivationType.VOICE.equals(next);
        } catch (Throwable t) {
            RaufixVoiceChanger.LOGGER.warn("Could not switch microphone activation mode", t);
            return false;
        }
    }

    /** Reloads Simple Voice Chat's OpenAL sound manager & microphone thread safely on the render thread. */
    public static void reloadAudioSystem() {
        try {
            releaseActiveOfflineMic();
            Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> {
                try {
                    ClientVoicechat client = ClientManager.getClient();
                    if (client != null) {
                        client.reloadAudio();
                    }
                } catch (Throwable t) {
                    RaufixVoiceChanger.LOGGER.warn("Simple Voice Chat audio reload failed", t);
                }
            });
        } catch (Throwable t) {
            RaufixVoiceChanger.LOGGER.warn("Could not schedule audio reload", t);
        }
    }

    /** Opens Simple Voice Chat's native settings screen, returning to {@code parent} on Back. */
    public static void openVoiceChatSettings(Screen parent) {
        try {
            ClientScreens.open(new VoiceChatSettingsScreen(parent));
        } catch (Throwable t) {
            RaufixVoiceChanger.LOGGER.warn("Could not open Simple Voice Chat settings screen", t);
        }
    }

    /** Called when Simple Voice Chat connects/disconnects so any offline mic yields immediately. */
    public static void releaseActiveOfflineMic() {
        OfflineMicSession session = activeOfflineMic;
        if (session != null) {
            session.close();
        }
    }

    /**
     * Opens an OpenAL speaker attached to Simple Voice Chat's active {@link SoundManager}
     * (or a temporary standalone {@link SoundManager} when offline). Never touches
     * {@code javax.sound.sampled} and never closes Simple Voice Chat's shared {@link SoundManager}.
     */
    public static SpeakerSession openSpeaker() throws SpeakerException {
        ClientVoicechat client = ClientManager.getClient();
        SoundManager soundManager = client != null ? client.getSoundManager() : null;
        boolean ownSoundManager = (soundManager == null || soundManager.isClosed());
        if (ownSoundManager) {
            soundManager = SoundManager.create();
        }
        try {
            Speaker speaker = SpeakerManager.createSpeaker(soundManager, null);
            return new SpeakerSession(soundManager, speaker, ownSoundManager);
        } catch (Throwable t) {
            if (ownSoundManager && soundManager != null) {
                soundManager.close();
            }
            if (t instanceof SpeakerException se) {
                throw se;
            }
            throw new SpeakerException("Failed to open OpenAL speaker", t);
        }
    }

    /**
     * Opens a temporary microphone capture session ONLY when Simple Voice Chat's
     * upstream {@link MicThread} is not actively polling (e.g., Singleplayer / Main Menu
     * or when Voice Chat is temporarily marked disabled).
     */
    public static OfflineMicSession openOfflineMic(Consumer<Exception> onError) {
        releaseActiveOfflineMic();
        ClientVoicechat client = ClientManager.getClient();
        MicThread existing = client != null ? client.getMicThread() : null;
        OfflineMicSession session;
        if (existing != null && !existing.isClosed()) {
            // Voice chat is disabled, so processMicrophone() is sleeping; borrow it cleanly like MicTestButton.
            existing.setMicrophoneLocked(true);
            session = new OfflineMicSession(existing, false);
        } else {
            MicThread own = new MicThread(client, null, ex -> {
                if (onError != null) {
                    onError.accept(ex);
                }
            });
            own.setMicrophoneLocked(true);
            own.start();
            session = new OfflineMicSession(own, true);
        }
        activeOfflineMic = session;
        return session;
    }

    public static final class SpeakerSession implements AutoCloseable {
        private final SoundManager soundManager;
        private final Speaker speaker;
        private final boolean ownSoundManager;
        private volatile boolean closed;

        SpeakerSession(SoundManager soundManager, Speaker speaker, boolean ownSoundManager) {
            this.soundManager = soundManager;
            this.speaker = speaker;
            this.ownSoundManager = ownSoundManager;
        }

        public boolean isHealthy() {
            if (this.closed || this.soundManager.isClosed()) {
                return false;
            }
            if (this.ownSoundManager) {
                ClientVoicechat client = ClientManager.getClient();
                if (client != null && client.getSoundManager() != null && !client.getSoundManager().isClosed()) {
                    // Shared client SoundManager became available; switch to it cleanly.
                    return false;
                }
            }
            return true;
        }

        /**
         * Submits one 20 ms (960-sample) mono 48 kHz frame to OpenAL.
         * Allocates an isolated 960-sample frame because {@code ALSpeakerBase}
         * queues buffers asynchronously on its sound-source executor.
         */
        public void playFrame(short[] samples, int length) {
            if (this.closed || samples == null || length <= 0 || this.soundManager.isClosed()) {
                return;
            }
            short[] frame = new short[FRAME_SAMPLES];
            System.arraycopy(samples, 0, frame, 0, Math.min(length, FRAME_SAMPLES));

            float configuredGain = 1.0f;
            if (VoicechatClient.CLIENT_CONFIG != null) {
                configuredGain = VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue();
            }
            float gain = Math.max(0.20f, Math.min(this.soundManager.getMaxGain(), configuredGain));
            this.speaker.play(frame, gain, null);
        }

        @Override
        public synchronized void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            try {
                this.speaker.close();
            } catch (Throwable ignored) {
            }
            if (this.ownSoundManager) {
                try {
                    this.soundManager.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public static final class OfflineMicSession implements AutoCloseable {
        private final MicThread micThread;
        private final boolean ownsThread;
        private volatile boolean closed;

        OfflineMicSession(MicThread micThread, boolean ownsThread) {
            this.micThread = micThread;
            this.ownsThread = ownsThread;
        }

        /**
         * True when a live server {@link MicThread} is now polling on its own and
         * this fallback session should yield immediately.
         */
        public boolean shouldYield() {
            if (this.closed || this.micThread.isClosed()) {
                return true;
            }
            ClientVoicechat client = ClientManager.getClient();
            if (this.ownsThread) {
                return client != null && (client.getMicThread() != null || client.getConnection() != null);
            }
            return !ClientManager.getPlayerStateManager().isDisabled();
        }

        public short[] pollFrame() {
            if (this.closed || this.micThread.isClosed()) {
                return null;
            }
            return this.micThread.pollProcessedAudio(true);
        }

        @Override
        public synchronized void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            if (activeOfflineMic == this) {
                activeOfflineMic = null;
            }
            try {
                if (this.ownsThread) {
                    this.micThread.close();
                    this.micThread.join(350L);
                } else {
                    this.micThread.setMicrophoneLocked(false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable ignored) {
            }
        }
    }
}
