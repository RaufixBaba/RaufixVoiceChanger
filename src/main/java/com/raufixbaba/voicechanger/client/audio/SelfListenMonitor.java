package com.raufixbaba.voicechanger.client.audio;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.model.ActiveVoice;
import com.raufixbaba.voicechanger.integration.OpenAlAudioBridge;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Live self-listen monitor ("Canlı Dinleme") powered 100% by Simple Voice Chat's
 * native OpenAL {@code SoundManager} + {@code SpeakerManager} + {@code MicThread}.
 *
 * <p>Zero {@code javax.sound.sampled} calls are made anywhere: this eliminates
 * ALSA / OpenALSoft device contention on Android (MojoLauncher / PojavLauncher),
 * Linux, macOS, and Windows, so game audio never cuts out and Simple Voice Chat's
 * microphone stream never disconnects.</p>
 *
 * <p>Works seamlessly in both Studio/Picker menus and live in-game ({@code I} key):</p>
 * <ul>
 *   <li>When actively transmitting to the server, plays the already-processed
 *       frames from {@code processedBus} (zero double-processing).</li>
 *   <li>When connected to a server without transmitting (e.g., inside GUI menus,
 *       PTT not held, or muted), transforms untransmitted live mic frames tapped
 *       directly from {@code MicThread} via {@code rawMicBus} (zero extra mic opened).</li>
 *   <li>When offline / singleplayer / voice chat disabled, borrows or opens an
 *       OpenAL {@code MicThread} via {@link OpenAlAudioBridge#openOfflineMic} and
 *       yields immediately whenever a server connection starts.</li>
 * </ul>
 */
public final class SelfListenMonitor {
    private static final int BLOCK_SAMPLES = OpenAlAudioBridge.FRAME_SAMPLES;
    private static final long IDLE_SLEEP_MILLIS = 3L;
    private static final long SPEAKER_RETRY_MILLIS = 200L;

    /** Where the monitored audio is coming from. */
    public enum Mode {
        STOPPED,
        /** Playing back what the outgoing voice chat processor produced. */
        SHARED_STREAM,
        /** Transforming untransmitted frames or offline OpenAL mic capture. */
        OWN_CAPTURE,
        /** Waiting for OpenAL audio device availability. */
        UNAVAILABLE
    }

    public interface AiPreview {
        boolean isActive();

        /** @return the converted frame, or {@code null} for silence */
        short[] convert(short[] mono);
    }

    private final SelfListenBus processedBus;
    private final SelfListenBus rawMicBus;
    private final Supplier<ActiveVoice> activeVoice;
    private final Consumer<Double> levelReporter;
    private final Consumer<Exception> failureHandler;
    private final AiPreview aiPreview;
    private final VoiceProcessor processor = new VoiceProcessor();

    private volatile int lastBlockFrames;
    private volatile int blocksPerSecond;
    private int blocksThisSecond;
    private volatile long secondStartedAt = System.currentTimeMillis();

    private Thread playbackThread;
    private volatile boolean running;
    private volatile Mode mode = Mode.STOPPED;

    public SelfListenMonitor(
            SelfListenBus processedBus,
            SelfListenBus rawMicBus,
            Supplier<ActiveVoice> activeVoice,
            Consumer<Double> levelReporter,
            Consumer<Exception> failureHandler,
            AiPreview aiPreview
    ) {
        this.processedBus = processedBus;
        this.rawMicBus = rawMicBus;
        this.activeVoice = activeVoice;
        this.levelReporter = levelReporter;
        this.failureHandler = failureHandler;
        this.aiPreview = aiPreview;
    }

    public Mode mode() {
        return this.mode;
    }

    public boolean isRunning() {
        return this.running;
    }

    /** What the preview chain is doing, for the studio spectrum & Hz telemetry. */
    public VoiceDiagnostics diagnostics() {
        if (!this.running || this.mode == Mode.SHARED_STREAM) {
            return VoiceDiagnostics.IDLE;
        }

        return new VoiceDiagnostics(
                this.processor.speakerPitchHz(),
                this.processor.appliedPitchRatio(),
                this.lastBlockFrames,
                1,
                recentBlocksPerSecond(),
                this.processor.spectrumBands());
    }

    private int recentBlocksPerSecond() {
        return System.currentTimeMillis() - this.secondStartedAt > 2_000L ? 0 : this.blocksPerSecond;
    }

    public synchronized void start() {
        if (this.running) {
            return;
        }

        this.running = true;
        this.processedBus.open();
        this.rawMicBus.open();
        this.processor.reset();
        this.playbackThread = new Thread(this::runPlayback, "raufix-voice-changer-self-listen");
        this.playbackThread.setDaemon(true);
        this.playbackThread.start();
    }

    public synchronized void stop() {
        this.running = false;
        this.processedBus.close();
        this.rawMicBus.close();
        Thread thread = this.playbackThread;
        this.playbackThread = null;
        if (thread != null && thread != Thread.currentThread()) {
            thread.interrupt();
        }
        this.mode = Mode.STOPPED;
        this.blocksPerSecond = 0;
    }

    private void runPlayback() {
        OpenAlAudioBridge.SpeakerSession speaker = null;
        OpenAlAudioBridge.OfflineMicSession offlineMic = null;
        short[] frame = new short[BLOCK_SAMPLES];
        boolean lastWasShared = false;
        boolean loggedSpeakerRetry = false;

        try {
            while (this.running && !Thread.currentThread().isInterrupted()) {
                // 1. Ensure a healthy OpenAL Speaker is open (auto-recovers across reloadAudio())
                if (speaker == null || !speaker.isHealthy()) {
                    closeQuietly(speaker);
                    speaker = null;
                    try {
                        speaker = OpenAlAudioBridge.openSpeaker();
                        loggedSpeakerRetry = false;
                    } catch (Exception ex) {
                        this.mode = Mode.UNAVAILABLE;
                        if (!loggedSpeakerRetry) {
                            loggedSpeakerRetry = true;
                            RaufixVoiceChanger.LOGGER.warn("Self-listen waiting for OpenAL speaker", ex);
                        }
                        Thread.sleep(SPEAKER_RETRY_MILLIS);
                        continue;
                    }
                }

                // 2. Priority 1: Already-processed frames from outgoing Simple Voice Chat transmission
                if (this.processedBus.readFullFrame(frame)) {
                    offlineMic = closeOfflineQuietly(offlineMic);
                    this.rawMicBus.clear();
                    if (!lastWasShared) {
                        this.processor.reset();
                        lastWasShared = true;
                    }
                    this.mode = Mode.SHARED_STREAM;
                    countBlock(BLOCK_SAMPLES);
                    speaker.playFrame(frame, BLOCK_SAMPLES);
                    continue;
                }

                // 3. Priority 2: Untransmitted live mic frames tapped from Simple Voice Chat's MicThread
                if (OpenAlAudioBridge.hasActiveUpstreamMicPoll()) {
                    offlineMic = closeOfflineQuietly(offlineMic);
                    if (this.rawMicBus.readFullFrame(frame)) {
                        if (lastWasShared) {
                            this.processor.reset();
                            lastWasShared = false;
                        }
                        this.mode = Mode.OWN_CAPTURE;
                        processAndPlayPreview(speaker, frame);
                        continue;
                    }
                    Thread.sleep(IDLE_SLEEP_MILLIS);
                    continue;
                }

                // 4. Priority 3: Offline / Singleplayer / Disabled Voice Chat fallback OpenAL Mic
                if (offlineMic != null && offlineMic.shouldYield()) {
                    offlineMic = closeOfflineQuietly(offlineMic);
                }
                if (offlineMic == null) {
                    try {
                        offlineMic = OpenAlAudioBridge.openOfflineMic(err ->
                                RaufixVoiceChanger.LOGGER.warn("Offline self-listen microphone error", err));
                        this.processor.reset();
                        lastWasShared = false;
                    } catch (RuntimeException ex) {
                        this.mode = Mode.UNAVAILABLE;
                        Thread.sleep(SPEAKER_RETRY_MILLIS);
                        continue;
                    }
                }

                short[] polled = offlineMic.pollFrame();
                if (polled == null || polled.length == 0) {
                    Thread.sleep(IDLE_SLEEP_MILLIS);
                    continue;
                }

                this.mode = Mode.OWN_CAPTURE;
                int copyLen = Math.min(BLOCK_SAMPLES, polled.length);
                System.arraycopy(polled, 0, frame, 0, copyLen);
                if (copyLen < BLOCK_SAMPLES) {
                    java.util.Arrays.fill(frame, copyLen, BLOCK_SAMPLES, (short) 0);
                }
                processAndPlayPreview(speaker, frame);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            this.mode = Mode.UNAVAILABLE;
            RaufixVoiceChanger.LOGGER.error("Self-listen monitor stopped unexpectedly", exception);
            if (this.failureHandler != null) {
                this.failureHandler.accept(exception);
            }
        } catch (LinkageError error) {
            this.mode = Mode.UNAVAILABLE;
            RaufixVoiceChanger.LOGGER.error("Self-listen linkage error", error);
            if (this.failureHandler != null) {
                this.failureHandler.accept(new IllegalStateException(error.getMessage(), error));
            }
        } finally {
            closeOfflineQuietly(offlineMic);
            closeQuietly(speaker);
            this.running = false;
            if (this.mode != Mode.UNAVAILABLE) {
                this.mode = Mode.STOPPED;
            }
        }
    }

    private void processAndPlayPreview(OpenAlAudioBridge.SpeakerSession speaker, short[] samples) {
        this.levelReporter.accept(peakOf(samples, BLOCK_SAMPLES));

        if (this.aiPreview.isActive()) {
            short[] converted = this.aiPreview.convert(samples);
            countBlock(BLOCK_SAMPLES);
            if (converted != null && converted.length > 0) {
                speaker.playFrame(converted, converted.length);
            }
            return;
        }

        ActiveVoice voice = this.activeVoice.get();
        if (voice.active()) {
            this.processor.process(samples, BLOCK_SAMPLES, 1, voice.profile(), voice.strength());
        } else {
            // Even when voice changer is bypassed, keep pitch/spectrum analyzer alive for Studio UI
            this.processor.process(samples, BLOCK_SAMPLES, 1, voice.profile(), 0);
        }
        countBlock(BLOCK_SAMPLES);
        speaker.playFrame(samples, BLOCK_SAMPLES);
    }

    private void countBlock(int frames) {
        this.lastBlockFrames = frames;
        this.blocksThisSecond++;

        long now = System.currentTimeMillis();
        if (now - this.secondStartedAt >= 1_000L) {
            this.blocksPerSecond = this.blocksThisSecond;
            this.blocksThisSecond = 0;
            this.secondStartedAt = now;
        }
    }

    public static double peakOf(short[] samples, int count) {
        int peak = 0;
        int limit = Math.min(samples.length, count);
        for (int i = 0; i < limit; i++) {
            int magnitude = Math.abs((int) samples[i]);
            if (magnitude > peak) {
                peak = magnitude;
            }
        }
        return Math.min(1.0D, peak / 32_768.0D);
    }

    private static void closeQuietly(OpenAlAudioBridge.SpeakerSession speaker) {
        if (speaker != null) {
            speaker.close();
        }
    }

    private static OpenAlAudioBridge.OfflineMicSession closeOfflineQuietly(OpenAlAudioBridge.OfflineMicSession mic) {
        if (mic != null) {
            mic.close();
        }
        return null;
    }
}
