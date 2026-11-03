package com.raufixbaba.voicechanger.client.ui;

import com.raufixbaba.voicechanger.client.VoiceChangerController;
import com.raufixbaba.voicechanger.client.audio.VoiceDiagnostics;
import com.raufixbaba.voicechanger.client.compat.ClientScreens;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import com.raufixbaba.voicechanger.client.model.VoiceChangerPreset;
import com.raufixbaba.voicechanger.client.model.VoiceParameter;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import com.raufixbaba.voicechanger.client.ui.compat.CompatScreen;
import com.raufixbaba.voicechanger.client.ui.compat.StudioGraphics;
import com.raufixbaba.voicechanger.client.ui.widget.NeonButton;
import com.raufixbaba.voicechanger.client.ui.widget.ParameterSlider;
import com.raufixbaba.voicechanger.client.ui.widget.StrengthSlider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Raufix Voice Studio V1.0 BETA — Animated Cyberpunk RGB Vocal Console.
 *
 * <p>Features a dedicated 1-click button to open {@link VoicePickerScreen}
 * (displaying all 22+ voices at once), a live 16-band RGB vocal spectrum
 * analyzer, self-listen monitor, personal preset manager, and 6 categorized
 * DSP parameter banks including the new Vocal Realism Engine.</p>
 */
public final class VoiceChangerStudioScreen extends CompatScreen {
    private static final int KEY_PAGE_UP = 266;
    private static final int KEY_PAGE_DOWN = 267;
    private static final int SLIDER_ROW_STEP = 23;

    private enum DspSection {
        CORE("raufixvc.dsp.tab.core",
                VoiceParameter.MIX,
                VoiceParameter.GAIN,
                VoiceParameter.PITCH,
                VoiceParameter.FORMANT,
                VoiceParameter.VOICE_MATCH),
        REALISM("raufixvc.dsp.tab.realism",
                VoiceParameter.CLARITY,
                VoiceParameter.BREATHINESS,
                VoiceParameter.WARMTH,
                VoiceParameter.CHORUS),
        TONE("raufixvc.dsp.tab.tone",
                VoiceParameter.GATE,
                VoiceParameter.DE_ESS,
                VoiceParameter.LOW_EQ,
                VoiceParameter.MID_EQ,
                VoiceParameter.HIGH_EQ,
                VoiceParameter.RADIO),
        CHARACTER("raufixvc.dsp.tab.character",
                VoiceParameter.GROWL,
                VoiceParameter.ROBOT_MIX,
                VoiceParameter.ROBOT_FREQUENCY,
                VoiceParameter.DISTORTION,
                VoiceParameter.BIT_DEPTH,
                VoiceParameter.NOISE,
                VoiceParameter.TREMOLO_DEPTH,
                VoiceParameter.TREMOLO_RATE),
        SPACE("raufixvc.dsp.tab.space",
                VoiceParameter.REVERB_MIX,
                VoiceParameter.REVERB_SIZE,
                VoiceParameter.REVERB_DECAY),
        AUTOTUNE("raufixvc.dsp.tab.autotune",
                VoiceParameter.AUTOTUNE_MIX,
                VoiceParameter.AUTOTUNE_SPEED,
                VoiceParameter.AUTOTUNE_KEY,
                VoiceParameter.AUTOTUNE_SCALE);

        final String labelKey;
        final VoiceParameter[] parameters;

        DspSection(String labelKey, VoiceParameter... parameters) {
            this.labelKey = labelKey;
            this.parameters = parameters;
        }
    }

    private final Screen parent;
    private final VoiceChangerController controller;
    private final ScrollPanel rightScroll = new ScrollPanel();
    private final List<ParameterSlider> parameterSliders = new ArrayList<>();

    private static DspSection activeSection = DspSection.CORE;
    private String presetNameDraft = "";
    private Component transientFeedback;
    private long feedbackUntilMillis;

    private NeonButton enableButton;
    private NeonButton selfListenButton;
    private NeonButton openPickerButton;
    private NeonButton savePresetButton;
    private NeonButton deletePresetButton;
    private NeonButton resetVoiceButton;
    private StrengthSlider strengthSlider;
    private EditBox presetNameBox;

    // Computed layout coordinates for custom neon panel painting
    private boolean twoColumn;
    private int leftX;
    private int leftW;
    private int heroY;
    private int heroH;
    private int rightX;
    private int rightW;
    private int rightPanelY;
    private int rightPanelBottom;

    public VoiceChangerStudioScreen(Screen parent, VoiceChangerController controller) {
        super(RaufixI18n.tr("raufixvc.studio.title"));
        this.parent = parent;
        this.controller = controller;
    }

    @Override
    protected void init() {
        this.controller.reloadSavedPresetNames();
        if (this.presetNameBox != null) {
            this.presetNameDraft = this.presetNameBox.getValue();
        } else if (this.presetNameDraft.isEmpty() && this.controller.getSelectedSavedPresetName() != null) {
            this.presetNameDraft = this.controller.getSelectedSavedPresetName();
        }

        int savedScroll = this.rightScroll.scrollOffset();
        clearWidgets();
        this.rightScroll.clear();
        this.parameterSliders.clear();

        // Shared Top Chrome + Language/Theme pills
        StudioFrame.addTabs(
                StudioTabs.of(StudioTabs.Tab.EFFECTS,
                        tab -> StudioTabs.navigate(tab, StudioTabs.Tab.EFFECTS, this.parent, this.controller)),
                this.font::width,
                this::addRenderableWidget
        );
        StudioFrame.addHeaderQuickControls(this.width, this.controller, this::init, this::addRenderableWidget);

        int contentTop = StudioFrame.listTop() + 6;
        int contentBottom = StudioFrame.listBottom(this.height) - 6;
        int totalW = Math.min(580, this.width - StudioFrame.MARGIN * 2);
        int startX = (this.width - totalW) / 2;

        this.twoColumn = totalW >= 390 && (contentBottom - contentTop) >= 156;
        if (this.twoColumn) {
            this.leftW = Math.max(176, Math.min(238, totalW * 42 / 100));
            this.leftX = startX;
            this.rightX = startX + this.leftW + 10;
            this.rightW = totalW - this.leftW - 10;
            buildLeftColumn(contentTop, contentBottom);
            buildRightColumn(contentTop, contentBottom, savedScroll);
        } else {
            this.leftX = startX;
            this.leftW = totalW;
            buildCompactLayout(contentTop, contentBottom, savedScroll);
        }

        // Footer Done button
        int footerY = StudioFrame.footerY(this.height);
        addRenderableWidget(new NeonButton(startX + totalW - 88, footerY, 88, 20,
                RaufixI18n.tr("raufixvc.ui.done"), btn -> onClose())
                .style(NeonButton.Style.RGB_PRIMARY));

        refreshWidgetsFromState();
    }

    private void buildLeftColumn(int top, int bottom) {
        int availH = bottom - top;
        this.heroY = top;
        this.heroH = availH >= 205 ? 78 : 62;

        int y = this.heroY + this.heroH + 6;

        // 1-Click Dedicated All-Voices Selection Page Button (Replaces 1-by-1 cycling!)
        int voiceCount = VoiceChangerPreset.selectablePresets().size();
        this.openPickerButton = new NeonButton(
                this.leftX, y, this.leftW, 22,
                RaufixI18n.tr("raufixvc.studio.open_picker_count", voiceCount),
                btn -> ClientScreens.open(new VoicePickerScreen(this.parent, this.controller))
        ).style(NeonButton.Style.RGB_PRIMARY);
        this.openPickerButton.setTooltip(Tooltip.create(RaufixI18n.tr("raufixvc.studio.open_picker.desc")));
        addRenderableWidget(this.openPickerButton);
        y += 26;

        // Power & Self-Listen row
        int halfW = (this.leftW - 4) / 2;
        this.enableButton = new NeonButton(this.leftX, y, halfW, 20, Component.empty(), btn -> {
            this.controller.toggleEnabled();
            refreshWidgetsFromState();
        });
        addRenderableWidget(this.enableButton);

        this.selfListenButton = new NeonButton(this.leftX + halfW + 4, y, this.leftW - halfW - 4, 20,
                Component.empty(), btn -> {
            this.controller.setSelfListenEnabled(!this.controller.isSelfListenEnabled());
            refreshWidgetsFromState();
        });
        this.selfListenButton.setTooltip(Tooltip.create(RaufixI18n.tr("raufixvc.studio.self_listen.desc")));
        addRenderableWidget(this.selfListenButton);
        y += 24;

        // RGB Strength Slider
        this.strengthSlider = new StrengthSlider(this.leftX, y, this.leftW, 20,
                this.controller::getStrength,
                val -> {
                    this.controller.setStrength(val);
                    refreshWidgetsFromState();
                });
        addRenderableWidget(this.strengthSlider);
        y += 25;

        // Personal Preset Save / Reset / Folder controls (when vertical space allows)
        if (y + 42 <= bottom + 2) {
            int saveBtnW = 54;
            int boxW = Math.max(60, this.leftW - saveBtnW - 4);
            this.presetNameBox = new EditBox(this.font, this.leftX, y, boxW, 18,
                    RaufixI18n.tr("raufixvc.studio.preset_name"));
            this.presetNameBox.setMaxLength(36);
            this.presetNameBox.setHint(RaufixI18n.tr("raufixvc.studio.preset_name.hint"));
            this.presetNameBox.setValue(this.presetNameDraft);
            addRenderableWidget(this.presetNameBox);

            this.savePresetButton = new NeonButton(this.leftX + boxW + 4, y, saveBtnW, 18,
                    RaufixI18n.tr("raufixvc.studio.save"), btn -> saveCurrentPreset());
            addRenderableWidget(this.savePresetButton);
            y += 22;

            int thirdW = (this.leftW - 8) / 3;
            this.resetVoiceButton = new NeonButton(this.leftX, y, thirdW, 18,
                    RaufixI18n.tr("raufixvc.studio.reset"), btn -> resetSelectedVoice());
            addRenderableWidget(this.resetVoiceButton);

            this.deletePresetButton = new NeonButton(this.leftX + thirdW + 4, y, thirdW, 18,
                    RaufixI18n.tr("raufixvc.studio.delete"), btn -> deleteCurrentPreset())
                    .style(NeonButton.Style.DANGER);
            addRenderableWidget(this.deletePresetButton);

            int folderX = this.leftX + (thirdW + 4) * 2;
            NeonButton folderBtn = new NeonButton(folderX, y, this.leftX + this.leftW - folderX, 18,
                    RaufixI18n.tr("raufixvc.studio.folder"), btn -> openPresetFolder());
            addRenderableWidget(folderBtn);
        }
    }

    private void buildRightColumn(int top, int bottom, int savedScroll) {
        this.rightPanelY = top;
        this.rightPanelBottom = bottom;

        // 6 DSP Category Tabs in 2 neat rows of 3 so labels never cramp
        DspSection[] sections = DspSection.values();
        int cols = this.rightW >= 320 ? 6 : 3;
        int rows = (sections.length + cols - 1) / cols;
        int pad = 6;
        int innerW = this.rightW - pad * 2;
        int gap = 3;
        int tabY = top + 6;

        for (int r = 0; r < rows; r++) {
            int rowStart = r * cols;
            int rowCount = Math.min(cols, sections.length - rowStart);
            int eachW = (innerW - gap * (rowCount - 1)) / rowCount;
            int x = this.rightX + pad;
            for (int c = 0; c < rowCount; c++) {
                DspSection sec = sections[rowStart + c];
                int w = (c == rowCount - 1) ? (this.rightX + pad + innerW - x) : eachW;
                NeonButton tabBtn = new NeonButton(x, tabY, w, 17, RaufixI18n.tr(sec.labelKey), ignored -> {
                    activeSection = sec;
                    this.rightScroll.setScrollOffset(0);
                    init();
                });
                if (sec == activeSection) {
                    tabBtn.style(NeonButton.Style.TAB_SELECTED);
                }
                addRenderableWidget(tabBtn);
                x += w + gap;
            }
            tabY += 20;
        }

        int slidersTop = tabY + 4;
        int slidersBottom = bottom - 6;
        int sliderW = innerW - 8;
        int sy = slidersTop;

        for (VoiceParameter param : activeSection.parameters) {
            ParameterSlider slider = new ParameterSlider(
                    this.rightX + pad,
                    sy,
                    sliderW,
                    20,
                    param,
                    this.controller::getPlayerProfile,
                    updated -> {
                        this.controller.updateAiSettings(s -> s.withSelected(false));
                        this.controller.applyCustomProfile(updated);
                        refreshWidgetsFromState();
                    }
            );
            this.parameterSliders.add(slider);
            addRenderableWidget(this.rightScroll.add(slider));
            sy += SLIDER_ROW_STEP;
        }

        this.rightScroll.layout(slidersTop, Math.max(slidersTop + 22, slidersBottom));
        this.rightScroll.setScrollOffset(savedScroll);
    }

    private void buildCompactLayout(int top, int bottom, int savedScroll) {
        int y = top;
        int voiceCount = VoiceChangerPreset.selectablePresets().size();
        this.openPickerButton = new NeonButton(
                this.leftX, y, this.leftW, 20,
                RaufixI18n.tr("raufixvc.studio.open_picker_count", voiceCount),
                btn -> ClientScreens.open(new VoicePickerScreen(this.parent, this.controller))
        ).style(NeonButton.Style.RGB_PRIMARY);
        addRenderableWidget(this.openPickerButton);
        y += 24;

        int halfW = (this.leftW - 4) / 2;
        this.enableButton = new NeonButton(this.leftX, y, halfW, 18, Component.empty(), btn -> {
            this.controller.toggleEnabled();
            refreshWidgetsFromState();
        });
        addRenderableWidget(this.enableButton);

        this.selfListenButton = new NeonButton(this.leftX + halfW + 4, y, this.leftW - halfW - 4, 18,
                Component.empty(), btn -> {
            this.controller.setSelfListenEnabled(!this.controller.isSelfListenEnabled());
            refreshWidgetsFromState();
        });
        addRenderableWidget(this.selfListenButton);
        y += 22;

        this.strengthSlider = new StrengthSlider(this.leftX, y, this.leftW, 18,
                this.controller::getStrength, this.controller::setStrength);
        addRenderableWidget(this.strengthSlider);
        y += 22;

        this.rightX = this.leftX;
        this.rightW = this.leftW;
        buildRightColumn(y, bottom, savedScroll);
    }

    private void saveCurrentPreset() {
        String raw = this.presetNameBox != null ? this.presetNameBox.getValue().trim() : "";
        if (raw.isEmpty()) {
            raw = this.controller.selectedVoiceDisplayComponent().getString();
        }
        try {
            String saved = this.controller.saveCurrentPreset(raw);
            if (this.presetNameBox != null) {
                this.presetNameBox.setValue(saved);
            }
            showFeedback(RaufixI18n.tr("raufixvc.message.preset_saved", saved));
            refreshWidgetsFromState();
        } catch (IOException | IllegalArgumentException ex) {
            showFeedback(RaufixI18n.tr("raufixvc.message.preset_failed", ex.getMessage()));
        }
    }

    private void deleteCurrentPreset() {
        String target = this.controller.getSelectedSavedPresetName();
        if ((target == null || target.isBlank()) && this.presetNameBox != null) {
            target = this.presetNameBox.getValue().trim();
        }
        if (target == null || target.isBlank()) {
            return;
        }
        try {
            if (this.controller.deleteSavedPreset(target)) {
                showFeedback(RaufixI18n.tr("raufixvc.message.preset_deleted", target));
                refreshWidgetsFromState();
            }
        } catch (IOException ex) {
            showFeedback(RaufixI18n.tr("raufixvc.message.preset_failed", ex.getMessage()));
        }
    }

    private void resetSelectedVoice() {
        VoiceChangerPreset preset = this.controller.getSelectedPreset();
        if (preset != VoiceChangerPreset.CUSTOM) {
            this.controller.applyBuiltInPreset(preset);
        } else {
            this.controller.applyBuiltInPreset(VoiceChangerPreset.WOMAN);
        }
        showFeedback(RaufixI18n.tr("raufixvc.message.voice_reset"));
        refreshWidgetsFromState();
    }

    private void openPresetFolder() {
        try {
            this.controller.ensurePresetDirectory();
            LinkOpener.open(this.controller.getPresetDirectory().toString());
        } catch (IOException | RuntimeException ex) {
            showFeedback(RaufixI18n.tr("raufixvc.message.open_folder_failed", ex.getMessage()));
        }
    }

    private void showFeedback(Component msg) {
        this.transientFeedback = msg;
        this.feedbackUntilMillis = System.currentTimeMillis() + 4_000L;
    }

    private void refreshWidgetsFromState() {
        boolean serverAllowed = this.controller.isAllowedByServer();
        boolean handTuningAllowed = serverAllowed && this.controller.getVoiceRule().allowsHandTuning();

        if (this.enableButton != null) {
            boolean on = this.controller.isEffectEnabled();
            this.enableButton.setMessage(RaufixI18n.tr(on ? "raufixvc.picker.power.on" : "raufixvc.picker.power.off"));
            this.enableButton.style(on ? NeonButton.Style.ACTIVE_ON : NeonButton.Style.DEFAULT);
            this.enableButton.active = serverAllowed;
        }
        if (this.selfListenButton != null) {
            boolean listen = this.controller.isSelfListenEnabled();
            this.selfListenButton.setMessage(RaufixI18n.tr(
                    listen ? "raufixvc.picker.listen.on" : "raufixvc.picker.listen.off"));
            this.selfListenButton.style(listen ? NeonButton.Style.ACTIVE_ON : NeonButton.Style.DEFAULT);
        }
        if (this.strengthSlider != null) {
            this.strengthSlider.active = serverAllowed;
            this.strengthSlider.refresh();
        }
        for (ParameterSlider slider : this.parameterSliders) {
            if (slider.visible) {
                slider.active = handTuningAllowed;
            }
            slider.refresh(this.controller.getPlayerProfile());
        }
        if (this.savePresetButton != null) {
            this.savePresetButton.active = this.controller.getVoiceRule().allowsPersonalPresets();
        }
        if (this.deletePresetButton != null) {
            this.deletePresetButton.active = this.controller.getVoiceRule().allowsPersonalPresets()
                    && (this.controller.getSelectedSavedPresetName() != null
                    || (this.presetNameBox != null && !this.presetNameBox.getValue().isBlank()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        refreshWidgetsFromState();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.rightScroll.scrollBy(-(int) Math.round(verticalAmount * SLIDER_ROW_STEP * 2))) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected boolean onKeyPressed(int keyCode) {
        int step = switch (keyCode) {
            case KEY_PAGE_UP -> -this.rightScroll.viewportHeight();
            case KEY_PAGE_DOWN -> this.rightScroll.viewportHeight();
            default -> 0;
        };
        return step != 0 && this.rightScroll.scrollBy(step);
    }

    @Override
    protected void paintBehindWidgets(StudioGraphics graphics) {
        StudioFrame.paintBackground(graphics, this.width, this.height);

        if (this.twoColumn) {
            // Left Hero Voice Monitor Card
            graphics.drawNeonPanel(this.leftX, this.heroY, this.leftW, this.heroH,
                    this.controller.isEffectActive(), 0.0f);

            // Right DSP Parameter Bank Panel
            graphics.drawNeonPanel(this.rightX, this.rightPanelY, this.rightW,
                    this.rightPanelBottom - this.rightPanelY, false, 0.35f);
        } else {
            graphics.drawNeonPanel(this.rightX, this.rightPanelY, this.rightW,
                    this.rightPanelBottom - this.rightPanelY, false, 0.35f);
        }
    }

    @Override
    protected void paintAboveWidgets(StudioGraphics graphics, int mouseX, int mouseY) {
        StudioFrame.paintChrome(graphics, this.width, this.height, RaufixI18n.tr("raufixvc.studio.title"));

        if (this.twoColumn) {
            paintHeroCardContents(graphics);
        }

        this.rightScroll.renderScrollbar(graphics, this.rightX + this.rightW - 6);
        paintFooterStatus(graphics);
    }

    private void paintHeroCardContents(StudioGraphics graphics) {
        int pad = 8;
        int x = this.leftX + pad;
        int y = this.heroY + 6;

        // Active status pill
        boolean active = this.controller.isEffectActive();
        Component stateBadge = !this.controller.isAllowedByServer()
                ? RaufixI18n.tr("raufixvc.badge.blocked")
                : RaufixI18n.tr(active ? "raufixvc.badge.active" : "raufixvc.badge.bypass");
        int badgeBorder = !this.controller.isAllowedByServer()
                ? RaufixTheme.DANGER
                : (active ? RaufixTheme.SUCCESS : 0xFF6B7591);
        graphics.drawPillBadge(stateBadge, x, y, 0xCC0D1426, badgeBorder, 0xFFFFFFFF);

        // Selected Voice Title
        Component voiceTitle = this.controller.aiVoice().isSelected()
                ? RaufixI18n.tr("raufixvc.ai.active_label")
                : this.controller.selectedVoiceDisplayComponent();
        StudioPaint.line(graphics, voiceTitle, x, y + 16, this.leftW - pad * 2, 0xFFFFFFFF);

        // Telemetry or description line
        VoiceDiagnostics diag = this.controller.getDiagnostics();
        Component telemetry;
        if (diag.speakerPitchHz() > 40.0D) {
            telemetry = Component.literal(String.format("♪ %.0f Hz → %.0f Hz (x%.2f)",
                    diag.speakerPitchHz(), diag.targetPitchHz(), diag.appliedPitchRatio()));
        } else {
            telemetry = RaufixI18n.tr(this.controller.getSelectedPreset().descriptionKey());
        }
        StudioPaint.line(graphics, telemetry, x, y + 28, this.leftW - pad * 2, RaufixTheme.TEXT_MUTED);

        // Live 16-Band RGB Spectrum Visualizer inside Hero Card
        int specH = Math.max(16, this.heroH - 46);
        if (specH >= 14) {
            graphics.drawSpectrumVisualizer(
                    x, this.heroY + this.heroH - specH - 5,
                    this.leftW - pad * 2, specH,
                    diag, this.controller.getInputLevel(),
                    active || this.controller.isSelfListenEnabled()
            );
        }
    }

    private void paintFooterStatus(StudioGraphics graphics) {
        int footerTextY = StudioFrame.footerY(this.height) + 6;
        int maxW = Math.max(80, this.width - StudioFrame.MARGIN * 2 - 100);
        Component msg;
        int color;

        if (this.transientFeedback != null && System.currentTimeMillis() < this.feedbackUntilMillis) {
            msg = this.transientFeedback;
            color = RaufixTheme.SUCCESS;
        } else if (!this.controller.isAllowedByServer()) {
            String custom = this.controller.getServerDenialMessage();
            msg = custom != null && !custom.isBlank()
                    ? Component.literal(custom)
                    : RaufixI18n.tr("raufixvc.studio.server_blocked",
                            RaufixI18n.tr(this.controller.getServerDenialReason().translationKey()).getString());
            color = RaufixTheme.DANGER;
        } else if (this.controller.isSelfListenEnabled()) {
            msg = RaufixI18n.tr("raufixvc.studio.footer.self_listen_hint");
            color = RaufixTheme.SUCCESS;
        } else {
            msg = RaufixI18n.tr("raufixvc.studio.footer.hotkey_hint");
            color = RaufixTheme.TEXT_MUTED;
        }

        StudioPaint.line(graphics, msg, StudioFrame.MARGIN, footerTextY, maxW, color);
    }

    @Override
    public void onClose() {
        this.controller.setSelfListenEnabled(false);
        ClientScreens.open(this.parent);
    }
}
