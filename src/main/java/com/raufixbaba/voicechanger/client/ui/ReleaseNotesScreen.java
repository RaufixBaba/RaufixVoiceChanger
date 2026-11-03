package com.raufixbaba.voicechanger.client.ui;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.VoiceChangerController;
import com.raufixbaba.voicechanger.client.compat.ClientScreens;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import com.raufixbaba.voicechanger.client.ui.compat.CompatScreen;
import com.raufixbaba.voicechanger.client.ui.compat.StudioGraphics;
import com.raufixbaba.voicechanger.client.ui.widget.NeonButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * V1.0 BETA Release Notes, Realistic Voice Guide & Hotkey Reference screen.
 */
public final class ReleaseNotesScreen extends CompatScreen {
    private static final int KEY_PAGE_UP = 266;
    private static final int KEY_PAGE_DOWN = 267;

    private final Screen parent;
    private final VoiceChangerController addon;
    private SettingsList list;

    public ReleaseNotesScreen(Screen parent, VoiceChangerController addon) {
        super(RaufixI18n.tr("raufixvc.notes.title"));
        this.parent = parent;
        this.addon = addon;
    }

    @Override
    protected void init() {
        clearWidgets();

        StudioFrame.addTabs(
                StudioTabs.of(StudioTabs.Tab.RELEASE_NOTES,
                        tab -> StudioTabs.navigate(tab, StudioTabs.Tab.RELEASE_NOTES, this.parent, this.addon)),
                this.font::width,
                this::addRenderableWidget
        );
        StudioFrame.addHeaderQuickControls(this.width, this.addon, this::init, this::addRenderableWidget);

        this.list = new SettingsList(this.width, this::addRenderableWidget);
        this.list.category(Component.literal("✦ " + RaufixVoiceChanger.MOD_NAME + " — " + RaufixVoiceChanger.MOD_BADGE));
        this.list.text(() -> RaufixI18n.tr("raufixvc.notes.intro"), () -> StudioPaint.TEXT, 3);
        this.list.gap(4);

        this.list.category(RaufixI18n.tr("raufixvc.notes.sec.realism"));
        this.list.text(() -> RaufixI18n.tr("raufixvc.notes.body.realism"), () -> StudioPaint.TEXT, 4);
        this.list.gap(4);

        this.list.category(RaufixI18n.tr("raufixvc.notes.sec.voices"));
        this.list.text(() -> RaufixI18n.tr("raufixvc.notes.body.voices"), () -> StudioPaint.TEXT, 4);
        this.list.gap(4);

        this.list.category(RaufixI18n.tr("raufixvc.notes.sec.shortcuts"));
        this.list.text(() -> RaufixI18n.tr("raufixvc.notes.body.shortcuts"), () -> RaufixTheme.SUCCESS, 4);
        this.list.finish(this.height);

        int footerY = StudioFrame.footerY(this.height);
        addRenderableWidget(new NeonButton(this.width / 2 - 154, footerY, 150, 20,
                RaufixI18n.tr("raufixvc.studio.open_picker_btn"),
                btn -> ClientScreens.open(new VoicePickerScreen(this.parent, this.addon)))
                .style(NeonButton.Style.RGB_PRIMARY));
        addRenderableWidget(new NeonButton(this.width / 2 + 4, footerY, 150, 20,
                RaufixI18n.tr("raufixvc.ui.done"),
                btn -> onClose()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.list != null && this.list.scrollBy(-(int) Math.round(verticalAmount * SettingsList.ROW_HEIGHT * 2))) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected boolean onKeyPressed(int keyCode) {
        if (this.list == null) {
            return false;
        }
        int step = switch (keyCode) {
            case KEY_PAGE_UP -> -this.list.viewportHeight();
            case KEY_PAGE_DOWN -> this.list.viewportHeight();
            default -> 0;
        };
        return step != 0 && this.list.scrollBy(step);
    }

    @Override
    protected void paintBehindWidgets(StudioGraphics graphics) {
        StudioFrame.paintBackground(graphics, this.width, this.height);
    }

    @Override
    protected void paintAboveWidgets(StudioGraphics graphics, int mouseX, int mouseY) {
        if (this.list != null) {
            this.list.paint(graphics);
        }
        StudioFrame.paintChrome(graphics, this.width, this.height, RaufixI18n.tr("raufixvc.notes.title"));
    }

    @Override
    public void onClose() {
        this.addon.setSelfListenEnabled(false);
        ClientScreens.open(this.parent);
    }
}
