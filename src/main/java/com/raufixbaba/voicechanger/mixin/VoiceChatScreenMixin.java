package com.raufixbaba.voicechanger.mixin;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.MinecraftResourceAccess;
import com.raufixbaba.voicechanger.client.MinecraftScreenAccess;
import com.raufixbaba.voicechanger.client.VoiceChangerController;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import com.raufixbaba.voicechanger.client.ui.VoiceChangerStudioScreen;
import com.raufixbaba.voicechanger.client.ui.VoicePickerScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VoiceChatScreen.class)
public abstract class VoiceChatScreenMixin extends Screen {
    @Unique
    private static final int VOICE_CHAT_SCREEN_WIDTH = 195;
    @Unique
    private static final String STUDIO_BUTTON_SPRITE = "icons/micro";
    @Unique
    private static final String STUDIO_BUTTON_TEXTURE = "textures/icons/micro.png";

    protected VoiceChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"), require = 0)
    private void raufixvc$addStudioButton(CallbackInfo callbackInfo) {
        try {
            VoiceChatScreenBase voiceChatScreen = (VoiceChatScreenBase) (Object) this;
            int x = voiceChatScreen.getGuiLeft() + (VOICE_CHAT_SCREEN_WIDTH - 20) / 2;
            int y = voiceChatScreen.getGuiTop() + 21;

            ImageButton studioButton = raufixvc$createStudioButton(x, y);
            if (studioButton == null) {
                return;
            }
            VoiceChangerController controller = VoiceChangerController.INSTANCE;
            studioButton.setTooltip(Tooltip.create(
                    controller.isInitialized() && !controller.isAllowedByServer()
                            ? RaufixI18n.tr("raufixvc.studio.server_blocked",
                                    RaufixI18n.tr(controller.getServerDenialReason().translationKey()).getString())
                            : RaufixI18n.tr("raufixvc.menu.open_studio")));
            this.addRenderableWidget(studioButton);
        } catch (RuntimeException exception) {
            RaufixVoiceChanger.LOGGER.error(
                    "Unable to add the Raufix Voice Changer button; keeping the Simple Voice Chat screen usable",
                    exception
            );
        }
    }

    @Unique
    private String raufixvc$iconReference() {
        for (Object child : this.children()) {
            if (!(child instanceof ImageButton button)) {
                continue;
            }

            try {
                Field textureField = ImageButton.class.getDeclaredField("texture");
                textureField.trySetAccessible();
                Object texture = textureField.get(button);
                if (texture != null) {
                    return String.valueOf(texture).contains("textures/")
                            ? STUDIO_BUTTON_TEXTURE
                            : STUDIO_BUTTON_SPRITE;
                }
            } catch (NoSuchFieldException | IllegalAccessException | RuntimeException exception) {
                RaufixVoiceChanger.LOGGER.debug("Could not read a Simple Voice Chat icon reference", exception);
                break;
            }
        }

        return STUDIO_BUTTON_SPRITE;
    }

    @Unique
    private ImageButton raufixvc$createStudioButton(int x, int y) {
        for (Constructor<?> constructor : ImageButton.class.getDeclaredConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length != 4
                    || parameters[0] != int.class
                    || parameters[1] != int.class
                    || parameters[3] != ImageButton.PressAction.class) {
                continue;
            }

            Object icon = MinecraftResourceAccess.create(
                    parameters[2],
                    RaufixVoiceChanger.MOD_ID,
                    raufixvc$iconReference()
            );
            ImageButton.PressAction action = button -> {
                Minecraft mc = Minecraft.getInstance();
                Screen parent = (Screen) (Object) this;
                boolean shift = mc.hasShiftDown();
                MinecraftScreenAccess.show(
                        mc,
                        shift
                                ? new VoicePickerScreen(parent, VoiceChangerController.INSTANCE)
                                : new VoiceChangerStudioScreen(parent, VoiceChangerController.INSTANCE)
                );
            };
            try {
                constructor.trySetAccessible();
                return (ImageButton) constructor.newInstance(x, y, icon, action);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                RaufixVoiceChanger.LOGGER.error("Unable to create the Raufix Voice Changer button", exception);
                return null;
            }
        }

        RaufixVoiceChanger.LOGGER.error("Simple Voice Chat has no compatible four-argument image button constructor");
        return null;
    }
}
