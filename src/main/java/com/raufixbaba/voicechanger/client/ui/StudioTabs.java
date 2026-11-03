package com.raufixbaba.voicechanger.client.ui;

import com.raufixbaba.voicechanger.client.VoiceChangerController;
import com.raufixbaba.voicechanger.client.compat.ClientScreens;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Top navigation tabs shared across all Raufix Voice Changer V1.0 BETA screens.
 */
public final class StudioTabs {
    public enum Tab {
        VOICE_PICKER("raufixvc.studio.tab.picker"),
        EFFECTS("raufixvc.studio.tab.effects"),
        AI_VOICE("raufixvc.studio.tab.ai"),
        RELEASE_NOTES("raufixvc.studio.tab.notes");

        private final String translationKey;

        Tab(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return this.translationKey;
        }
    }

    private StudioTabs() {
    }

    public static List<StudioFrame.Tab> of(Tab current, Consumer<Tab> open) {
        List<StudioFrame.Tab> tabs = new ArrayList<>();
        for (Tab tab : Tab.values()) {
            tabs.add(new StudioFrame.Tab(
                    RaufixI18n.tr(tab.translationKey),
                    tab.translationKey + ".desc",
                    tab == current,
                    true,
                    () -> open.accept(tab)
            ));
        }
        return tabs;
    }

    public static void navigate(Tab target, Tab current, Screen parent, VoiceChangerController addon) {
        if (target == current) {
            return;
        }
        switch (target) {
            case VOICE_PICKER -> ClientScreens.open(new VoicePickerScreen(parent, addon));
            case EFFECTS -> ClientScreens.open(new VoiceChangerStudioScreen(parent, addon));
            case AI_VOICE -> ClientScreens.open(new AiVoiceScreen(parent, addon));
            case RELEASE_NOTES -> ClientScreens.open(new ReleaseNotesScreen(parent, addon));
        }
    }
}
