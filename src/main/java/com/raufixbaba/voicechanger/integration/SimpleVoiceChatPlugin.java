package com.raufixbaba.voicechanger.integration;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.VoiceChangerController;
import com.raufixbaba.voicechanger.client.audio.SimpleVoiceChatAudioProcessor.ProcessingResult;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class SimpleVoiceChatPlugin implements VoicechatPlugin {
    private static final int AUDIO_PROCESSING_PRIORITY = 100;

    @Override
    public String getPluginId() {
        return RaufixVoiceChanger.PLUGIN_ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        if (!isClient()) {
            return;
        }

        registration.registerEvent(
                ClientSoundEvent.class,
                this::onClientSound,
                AUDIO_PROCESSING_PRIORITY
        );
        registration.registerEvent(
                ClientVoicechatConnectionEvent.class,
                this::onConnectionChanged,
                0
        );
        RaufixVoiceChanger.LOGGER.info("Registered the Raufix Simple Voice Chat OpenAL audio processor");
    }

    private static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    private void onConnectionChanged(ClientVoicechatConnectionEvent event) {
        // Release any offline self-listen mic immediately so the server's MicThread owns MICROPHONE_LOCK cleanly.
        OpenAlAudioBridge.releaseActiveOfflineMic();
        if (VoiceChangerController.INSTANCE.isInitialized()) {
            VoiceChangerController.INSTANCE.getAudioProcessor().reset();
        }
    }

    private void onClientSound(ClientSoundEvent event) {
        try {
            processClientSound(event);
        } catch (RuntimeException exception) {
            VoiceChangerController.INSTANCE.getAudioProcessor().reset();
            RaufixVoiceChanger.LOGGER.error(
                    "Voice changer audio processing failed; sending the original microphone frame",
                    exception
            );
        }
    }

    private void processClientSound(ClientSoundEvent event) {
        VoiceChangerController controller = VoiceChangerController.INSTANCE;
        if (!controller.isInitialized()) {
            return;
        }

        short[] rawAudio = event.getRawAudio();
        if (rawAudio == null || rawAudio.length == 0) {
            return;
        }

        if (controller.isAiVoiceActive() && controller.api().winningOverrideOwner() == null) {
            controller.getAudioProcessor().countAiFrame(rawAudio.length);
            controller.publishAiFrame(rawAudio);
            return;
        }

        short[] output = controller.isEffectActive()
                ? rawAudio.clone()
                : rawAudio;
        ProcessingResult result = controller.getAudioProcessor().process(output, controller);

        if (result == ProcessingResult.STOP_ACTIVE_STREAM) {
            event.cancel();
            return;
        }

        if (result == ProcessingResult.PROCESSED) {
            event.setRawAudio(output);
        }
    }
}
