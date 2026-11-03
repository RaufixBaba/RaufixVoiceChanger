package com.raufixbaba.voicechanger.mixin;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.MinecraftScreenAccess;
import com.raufixbaba.voicechanger.client.VoiceChangerController;
import com.raufixbaba.voicechanger.client.ai.audio.AudioFrames;
import com.raufixbaba.voicechanger.client.audio.SimpleVoiceChatAudioProcessor;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.voice.client.MicThread;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MicThread.class)
public abstract class MicThreadMixin {
    @Unique
    private static final SimpleVoiceChatAudioProcessor MIC_TEST_PROCESSOR =
            SimpleVoiceChatAudioProcessor.forMicrophoneTest();
    @Unique
    private static long activeMicTestThreadId = -1L;
    @Unique
    private static final boolean HAS_PROCESSED_AUDIO_METHOD = hasProcessedAudioMethod();
    @Unique
    private static boolean hasLoggedMicTestFailure;

    @ModifyVariable(method = "sendAudio", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private short[] raufixvc$routeThroughAiVoice(short[] rawAudio) {
        VoiceChangerController controller = VoiceChangerController.INSTANCE;
        return controller.isInitialized() ? controller.onMicFrame(rawAudio) : rawAudio;
    }

    @Inject(method = "pollProcessedAudio", at = @At("RETURN"), cancellable = true, require = 0)
    private void raufixvc$processMicTestAudio(
            boolean forceProcessing,
            CallbackInfoReturnable<short[]> callbackInfo
    ) {
        if (!forceProcessing) {
            // Live server MicThread poll: tap untransmitted mic frames for OpenAL self-listen & studio meters
            // without ever locking or opening a second microphone!
            short[] samples = callbackInfo.getReturnValue();
            VoiceChangerController controller = VoiceChangerController.INSTANCE;
            if (samples != null && samples.length > 0 && controller.isInitialized()) {
                boolean willTransmit = ((MicThread) (Object) this).shouldTransmitAudio();
                controller.onLiveMicPoll(samples, willTransmit);
            }
            return;
        }

        processMicTestAudio(callbackInfo);
    }

    @Inject(method = "pollMic", at = @At("RETURN"), cancellable = true, require = 0)
    private void raufixvc$processLegacyMicTestAudio(CallbackInfoReturnable<short[]> callbackInfo) {
        if (HAS_PROCESSED_AUDIO_METHOD) {
            return;
        }

        processMicTestAudio(callbackInfo);
    }

    @Unique
    private static void processMicTestAudio(CallbackInfoReturnable<short[]> callbackInfo) {
        if (!(MinecraftScreenAccess.current(Minecraft.getInstance()) instanceof VoiceChatSettingsScreen)) {
            return;
        }

        short[] samples = callbackInfo.getReturnValue();
        VoiceChangerController controller = VoiceChangerController.INSTANCE;
        if (samples == null || samples.length == 0 || !controller.isInitialized()) {
            return;
        }

        long currentThreadId = Thread.currentThread().threadId();
        if (activeMicTestThreadId != currentThreadId) {
            MIC_TEST_PROCESSOR.reset();
            activeMicTestThreadId = currentThreadId;
        }

        if (controller.isAiVoiceActive() && samples.length == AudioFrames.FRAME_SAMPLES) {
            short[] converted = controller.aiVoice().previewFrame(samples);
            callbackInfo.setReturnValue(converted != null ? converted : AudioFrames.silence());
            return;
        }

        try {
            MIC_TEST_PROCESSOR.process(samples, controller);
            callbackInfo.setReturnValue(samples);
        } catch (RuntimeException exception) {
            MIC_TEST_PROCESSOR.reset();
            if (!hasLoggedMicTestFailure) {
                hasLoggedMicTestFailure = true;
                RaufixVoiceChanger.LOGGER.error(
                        "Voice changer microphone test failed; keeping the original microphone audio",
                        exception
                );
            }
        }
    }

    @Unique
    private static boolean hasProcessedAudioMethod() {
        try {
            MicThread.class.getDeclaredMethod("pollProcessedAudio", boolean.class);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }
}
