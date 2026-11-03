package com.raufixbaba.voicechanger.client.ai.audio;

import com.raufixbaba.voicechanger.integration.OpenAlAudioBridge;
import java.util.concurrent.locks.LockSupport;

/**
 * Plays a voice sample from the AI gallery to the player alone through Simple Voice Chat's
 * OpenAL speaker pipeline (zero {@code javax.sound.sampled} device contention).
 */
public final class PreviewPlayer {
    private static final long FRAME_NANOS = 20_000_000L; // 20 ms per 960-sample 48 kHz frame

    private static Thread playing;

    private PreviewPlayer() {
    }

    public static synchronized void play(short[] samples) {
        stop();
        if (samples == null || samples.length == 0) {
            return;
        }
        Thread thread = new Thread(() -> streamOpenAl(samples), "raufix-ai-preview-player");
        thread.setDaemon(true);
        playing = thread;
        thread.start();
    }

    public static synchronized void stop() {
        if (playing != null) {
            playing.interrupt();
            playing = null;
        }
    }

    private static void streamOpenAl(short[] samples) {
        try (OpenAlAudioBridge.SpeakerSession speaker = OpenAlAudioBridge.openSpeaker()) {
            int step = OpenAlAudioBridge.FRAME_SAMPLES;
            short[] frame = new short[step];
            long nextDeadline = System.nanoTime();

            for (int offset = 0; offset < samples.length && !Thread.currentThread().isInterrupted(); offset += step) {
                int count = Math.min(step, samples.length - offset);
                System.arraycopy(samples, offset, frame, 0, count);
                if (count < step) {
                    java.util.Arrays.fill(frame, count, step, (short) 0);
                }
                speaker.playFrame(frame, step);

                nextDeadline += FRAME_NANOS;
                long waitNanos = nextDeadline - System.nanoTime();
                if (waitNanos > 0L) {
                    LockSupport.parkNanos(waitNanos);
                }
            }
            // Allow OpenAL source queue to finish draining last buffers smoothly
            if (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(120L);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // Speaker unavailable; gallery stays silent without disturbing OpenAL.
        }
    }
}
