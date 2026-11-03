package com.raufixbaba.voicechanger;

import com.raufixbaba.voicechanger.network.VoiceChangerNetwork;
import com.raufixbaba.voicechanger.server.VoiceChangerCommand;
import com.raufixbaba.voicechanger.server.VoiceChangerServerRuntime;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class RaufixVoiceChangerFabricMain implements ModInitializer {
    @Override
    public void onInitialize() {
        VoiceChangerNetwork.initialize();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registry, environment) -> VoiceChangerCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                VoiceChangerServerRuntime.INSTANCE.start(server, FabricLoader.getInstance().getConfigDir()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
                VoiceChangerServerRuntime.INSTANCE.stop());

        RaufixVoiceChanger.LOGGER.info("{} {} initialized by {}",
                RaufixVoiceChanger.MOD_NAME, RaufixVoiceChanger.MOD_VERSION, RaufixVoiceChanger.MOD_AUTHOR);
    }
}
