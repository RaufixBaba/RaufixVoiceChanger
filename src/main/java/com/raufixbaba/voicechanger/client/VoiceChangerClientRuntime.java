package com.raufixbaba.voicechanger.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.ui.VoiceChangerStudioScreen;
import com.raufixbaba.voicechanger.client.ui.VoicePickerScreen;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class VoiceChangerClientRuntime {
    public static final VoiceChangerClientRuntime INSTANCE = new VoiceChangerClientRuntime();

    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(RaufixVoiceChanger.MOD_ID, "voice_changer")
    );

    private KeyMapping openPickerKey;
    private KeyMapping openStudioKey;
    private KeyMapping toggleEffectKey;
    private KeyMapping selfListenKey;
    private boolean initialized;

    private VoiceChangerClientRuntime() {
    }

    public void initialize(Path configDirectory) {
        if (this.initialized) {
            return;
        }

        VoiceChangerController.INSTANCE.initialize(configDirectory);

        this.openPickerKey = new KeyMapping(
                "key.raufixvc.open_picker",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_J,
                KEY_CATEGORY
        );
        this.openStudioKey = new KeyMapping(
                "key.raufixvc.open_studio",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_U,
                KEY_CATEGORY
        );
        this.toggleEffectKey = new KeyMapping(
                "key.raufixvc.toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_O,
                KEY_CATEGORY
        );
        this.selfListenKey = new KeyMapping(
                "key.raufixvc.self_listen",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_I,
                KEY_CATEGORY
        );

        this.initialized = true;
        RaufixVoiceChanger.LOGGER.info("Initialized {} {} client runtime (by {})",
                RaufixVoiceChanger.MOD_NAME, RaufixVoiceChanger.MOD_VERSION, RaufixVoiceChanger.MOD_AUTHOR);
    }

    public void registerHotkeys(Consumer<KeyMapping> registrar) {
        requireInitialized();
        registrar.accept(this.openPickerKey);
        registrar.accept(this.openStudioKey);
        registrar.accept(this.toggleEffectKey);
        registrar.accept(this.selfListenKey);
    }

    public void tick(Minecraft client) {
        if (!this.initialized) {
            return;
        }

        VoiceChangerController controller = VoiceChangerController.INSTANCE;
        controller.tick();

        while (this.openPickerKey.consumeClick()) {
            if (MinecraftScreenAccess.current(client) == null) {
                MinecraftScreenAccess.show(client, new VoicePickerScreen(null, controller));
            }
        }

        while (this.openStudioKey.consumeClick()) {
            if (MinecraftScreenAccess.current(client) == null) {
                MinecraftScreenAccess.show(client, new VoiceChangerStudioScreen(null, controller));
            }
        }

        while (this.toggleEffectKey.consumeClick()) {
            if (MinecraftScreenAccess.current(client) == null) {
                controller.toggleEnabled();
            }
        }

        while (this.selfListenKey.consumeClick()) {
            if (MinecraftScreenAccess.current(client) == null) {
                controller.toggleSelfListenInGame();
            }
        }
    }

    public void shutdown() {
        if (!this.initialized) {
            return;
        }

        VoiceChangerController.INSTANCE.shutdown();
        this.initialized = false;
    }

    private void requireInitialized() {
        if (!this.initialized) {
            throw new IllegalStateException("Raufix Voice Changer client runtime is not initialized");
        }
    }
}
