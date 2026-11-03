package com.raufixbaba.voicechanger.client.ui;

import com.raufixbaba.voicechanger.client.VoiceChangerController;
import com.raufixbaba.voicechanger.client.api.VoicePreset;
import com.raufixbaba.voicechanger.client.audio.VoiceDiagnostics;
import com.raufixbaba.voicechanger.client.compat.ClientScreens;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import com.raufixbaba.voicechanger.client.model.VoiceCategory;
import com.raufixbaba.voicechanger.client.model.VoiceChangerPreset;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import com.raufixbaba.voicechanger.client.ui.compat.CompatScreen;
import com.raufixbaba.voicechanger.client.ui.compat.StudioGraphics;
import com.raufixbaba.voicechanger.client.ui.widget.NeonButton;
import com.raufixbaba.voicechanger.client.ui.widget.StrengthSlider;
import com.raufixbaba.voicechanger.client.ui.widget.VoiceCardButton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Dedicated 1-Click All-Voices Selection Page ("Bütün Seslerin Olduğu Seçme Sayfası")
 * for Raufix Voice Changer V1.0 BETA.
 *
 * <p>Replaces tedious one-by-one button cycling with an animated RGB category-filtered,
 * searchable 2-column voice gallery with live self-listen preview.</p>
 */
public final class VoicePickerScreen extends CompatScreen {
    private static final int FILTER_BAR_HEIGHT = 48;
    private static final int PICKER_FOOTER_HEIGHT = 36;
    private static final int CARD_HEIGHT = 34;
    private static final int CARD_GAP_X = 8;
    private static final int CARD_GAP_Y = 6;
    private static final int KEY_PAGE_UP = 266;
    private static final int KEY_PAGE_DOWN = 267;

    private enum FilterTab {
        ALL("raufixvc.category.all", null),
        NATURAL("raufixvc.category.realistic", VoiceCategory.REALISTIC),
        CHARACTER("raufixvc.category.character", VoiceCategory.CHARACTER),
        FANTASY("raufixvc.category.fantasy", VoiceCategory.FANTASY),
        SCIFI("raufixvc.category.effects", VoiceCategory.EFFECTS),
        SAVED("raufixvc.category.saved", null);

        final String labelKey;
        final VoiceCategory category;

        FilterTab(String labelKey, VoiceCategory category) {
            this.labelKey = labelKey;
            this.category = category;
        }
    }

    private final Screen parent;
    private final VoiceChangerController addon;
    private final ScrollPanel cardScroll = new ScrollPanel();

    private static FilterTab activeFilter = FilterTab.ALL;
    private String searchQuery = "";
    private EditBox searchBox;
    private NeonButton enableButton;
    private NeonButton selfListenButton;
    private NeonButton closeOnPickButton;
    private StrengthSlider strengthSlider;
    private int gridLeft;
    private int gridWidth;
    private int listTopY;
    private int listBottomY;
    private int visibleCardCount;

    public VoicePickerScreen(Screen parent, VoiceChangerController addon) {
        super(RaufixI18n.tr("raufixvc.studio.title"));
        this.parent = parent;
        this.addon = addon;
    }

    @Override
    protected void init() {
        this.addon.reloadSavedPresetNames();
        int savedScroll = this.cardScroll.scrollOffset();
        clearWidgets();
        this.cardScroll.clear();

        // Top Navigation Chrome + Language/Theme pills
        StudioFrame.addTabs(
                StudioTabs.of(StudioTabs.Tab.VOICE_PICKER,
                        tab -> StudioTabs.navigate(tab, StudioTabs.Tab.VOICE_PICKER, this.parent, this.addon)),
                this.font::width,
                this::addRenderableWidget
        );
        StudioFrame.addHeaderQuickControls(this.width, this.addon, this::init, this::addRenderableWidget);

        this.gridWidth = Math.min(540, this.width - StudioFrame.MARGIN * 2);
        this.gridLeft = (this.width - this.gridWidth) / 2;

        // Row 1 of Filter Bar: Category Filter Pills
        int filterRowY = StudioFrame.HEADER_HEIGHT + 5;
        buildCategoryFilterRow(filterRowY);

        // Row 2 of Filter Bar: Live Search Box + [Seçince Kapat] toggle
        int searchRowY = filterRowY + 21;
        int closeToggleW = Math.min(148, Math.max(108, this.gridWidth / 3));
        int searchW = Math.max(90, this.gridWidth - closeToggleW - 6);

        this.searchBox = new EditBox(this.font, this.gridLeft, searchRowY, searchW, 18,
                RaufixI18n.tr("raufixvc.picker.search"));
        this.searchBox.setMaxLength(48);
        this.searchBox.setHint(RaufixI18n.tr("raufixvc.picker.search.hint"));
        this.searchBox.setValue(this.searchQuery);
        this.searchBox.setResponder(query -> {
            if (!query.equals(this.searchQuery)) {
                this.searchQuery = query;
                rebuildCardsOnly();
            }
        });
        addRenderableWidget(this.searchBox);

        this.closeOnPickButton = new NeonButton(
                this.gridLeft + searchW + 6, searchRowY, closeToggleW, 18,
                Component.empty(),
                btn -> {
                    if (this.addon.uiConfig() != null) {
                        this.addon.uiConfig().setClosePickerOnSelect(!this.addon.uiConfig().closePickerOnSelect());
                    }
                    refreshControlLabels();
                }
        );
        this.closeOnPickButton.setTooltip(Tooltip.create(RaufixI18n.tr("raufixvc.picker.auto_close.desc")));
        addRenderableWidget(this.closeOnPickButton);

        // Scrollable 2-column RGB Voice Card Grid
        this.listTopY = StudioFrame.HEADER_HEIGHT + FILTER_BAR_HEIGHT;
        this.listBottomY = Math.max(this.listTopY + 42, this.height - PICKER_FOOTER_HEIGHT);
        populateCards();
        this.cardScroll.layout(this.listTopY, this.listBottomY);
        this.cardScroll.setScrollOffset(savedScroll);

        // Live Studio Footer Controls
        buildFooter();
        refreshControlLabels();
    }

    private void rebuildCardsOnly() {
        this.searchQuery = this.searchBox != null ? this.searchBox.getValue() : this.searchQuery;
        boolean searchFocused = this.searchBox != null && this.searchBox.isFocused();
        int cursorPos = this.searchBox != null ? this.searchBox.getCursorPosition() : 0;
        init();
        if (searchFocused && this.searchBox != null) {
            setFocused(this.searchBox);
            this.searchBox.setFocused(true);
            this.searchBox.setCursorPosition(cursorPos);
        }
    }

    private void buildCategoryFilterRow(int y) {
        FilterTab[] tabs = FilterTab.values();
        int gap = 4;
        int eachW = (this.gridWidth - gap * (tabs.length - 1)) / tabs.length;
        int x = this.gridLeft;

        for (int i = 0; i < tabs.length; i++) {
            FilterTab tab = tabs[i];
            int w = (i == tabs.length - 1) ? (this.gridLeft + this.gridWidth - x) : eachW;
            Component label = RaufixI18n.tr(tab.labelKey);
            NeonButton btn = new NeonButton(x, y, w, 18, label, ignored -> {
                activeFilter = tab;
                this.cardScroll.setScrollOffset(0);
                init();
            });
            if (tab == activeFilter) {
                btn.style(NeonButton.Style.TAB_SELECTED);
            }
            addRenderableWidget(btn);
            x += w + gap;
        }
    }

    private void populateCards() {
        List<CardSpec> specs = collectMatchingCards();
        this.visibleCardCount = specs.size();

        int cols = this.gridWidth >= 340 ? 2 : 1;
        int cardW = cols == 2 ? (this.gridWidth - CARD_GAP_X - 8) / 2 : (this.gridWidth - 8);
        int startY = this.listTopY + 4;

        for (int i = 0; i < specs.size(); i++) {
            CardSpec spec = specs.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = this.gridLeft + col * (cardW + CARD_GAP_X);
            int cy = startY + row * (CARD_HEIGHT + CARD_GAP_Y);

            VoiceCardButton card = new VoiceCardButton(
                    cx, cy, cardW, CARD_HEIGHT,
                    spec.title(),
                    spec.subtitle(),
                    spec.accentColor(),
                    spec.isNew(),
                    spec.selected()::get,
                    () -> {
                        spec.onPick().run();
                        if (!this.addon.isEffectEnabled() && this.addon.isAllowedByServer()) {
                            this.addon.setEnabled(true);
                        }
                        refreshControlLabels();
                        if (this.addon.uiConfig() != null
                                && this.addon.uiConfig().closePickerOnSelect()
                                && !this.addon.isSelfListenEnabled()) {
                            onClose();
                        }
                    }
            );
            card.active = spec.allowed();
            if (spec.subtitle() != null) {
                card.setTooltip(Tooltip.create(spec.subtitle()));
            }
            addRenderableWidget(this.cardScroll.add(card));
        }
    }

    private List<CardSpec> collectMatchingCards() {
        List<CardSpec> results = new ArrayList<>();
        String needle = this.searchQuery == null ? "" : this.searchQuery.trim().toLowerCase(Locale.ROOT);

        // Built-in 22 studio-tuned presets
        if (activeFilter != FilterTab.SAVED) {
            for (VoiceChangerPreset preset : VoiceChangerPreset.selectablePresets()) {
                if (activeFilter.category != null && preset.category() != activeFilter.category) {
                    continue;
                }
                Component rawTitle = RaufixI18n.tr(preset.translationKey());
                Component title = Component.literal(preset.iconSymbol() + " " + rawTitle.getString());
                Component desc = RaufixI18n.tr(preset.descriptionKey());
                if (!matchesSearch(needle, title.getString(), desc.getString(), preset.id())) {
                    continue;
                }
                boolean allowed = this.addon.getVoiceRule().allows(preset.id());
                results.add(new CardSpec(
                        title,
                        desc,
                        preset.category().accentColor(),
                        preset.isNewInV1(),
                        allowed,
                        () -> this.addon.getSelectedPreset() == preset && !this.addon.aiVoice().isSelected(),
                        () -> {
                            this.addon.updateAiSettings(s -> s.withSelected(false));
                            this.addon.applyBuiltInPreset(preset);
                        }
                ));
            }
        }

        // Personal saved presets & Server shared / API contributed presets
        if (activeFilter == FilterTab.ALL || activeFilter == FilterTab.SAVED) {
            for (VoicePreset shared : this.addon.api().contributedPresets().all()) {
                Component title = shared.displayName();
                Component desc = RaufixI18n.tr("raufixvc.picker.shared_voice.desc");
                if (!matchesSearch(needle, title.getString(), desc.getString(), shared.id())) {
                    continue;
                }
                boolean allowed = this.addon.getVoiceRule().allows(shared.id());
                results.add(new CardSpec(
                        title,
                        desc,
                        0xFF38EF7D,
                        false,
                        allowed,
                        () -> shared.id().equals(this.addon.getSelectedContributedVoiceId()),
                        () -> {
                            this.addon.updateAiSettings(s -> s.withSelected(false));
                            this.addon.applyContributedPreset(shared);
                        }
                ));
            }

            if (this.addon.getVoiceRule().allowsPersonalPresets()) {
                for (String savedName : this.addon.getSavedPresetNamesCached()) {
                    Component title = Component.literal("★ " + savedName);
                    Component desc = RaufixI18n.tr("raufixvc.picker.saved_voice.desc");
                    if (!matchesSearch(needle, savedName, desc.getString(), savedName)) {
                        continue;
                    }
                    results.add(new CardSpec(
                            title,
                            desc,
                            0xFFFFB84D,
                            false,
                            true,
                            () -> savedName.equalsIgnoreCase(String.valueOf(this.addon.getSelectedSavedPresetName())),
                            () -> {
                                try {
                                    this.addon.updateAiSettings(s -> s.withSelected(false));
                                    this.addon.loadSavedPreset(savedName);
                                } catch (IOException ignored) {
                                }
                            }
                    ));
                }
            }
        }

        return results;
    }

    private static boolean matchesSearch(String needle, String title, String desc, String id) {
        if (needle.isEmpty()) {
            return true;
        }
        return title.toLowerCase(Locale.ROOT).contains(needle)
                || desc.toLowerCase(Locale.ROOT).contains(needle)
                || id.toLowerCase(Locale.ROOT).contains(needle);
    }

    private void buildFooter() {
        int y = this.height - PICKER_FOOTER_HEIGHT + (PICKER_FOOTER_HEIGHT - 20) / 2;
        int gap = 6;
        int meterReserve = this.gridWidth >= 440 ? 74 : 0;
        int usableW = this.gridWidth - meterReserve - (meterReserve > 0 ? gap : 0);

        int doneW = 68;
        int toggleW = Math.max(92, (usableW - doneW - gap * 3) * 32 / 100);
        int listenW = Math.max(88, (usableW - doneW - gap * 3) * 30 / 100);
        int sliderW = Math.max(76, usableW - toggleW - listenW - doneW - gap * 3);

        int x = this.gridLeft;
        this.enableButton = new NeonButton(x, y, toggleW, 20, Component.empty(), btn -> {
            this.addon.toggleEnabled();
            refreshControlLabels();
        });
        addRenderableWidget(this.enableButton);
        x += toggleW + gap;

        this.selfListenButton = new NeonButton(x, y, listenW, 20, Component.empty(), btn -> {
            this.addon.setSelfListenEnabled(!this.addon.isSelfListenEnabled());
            refreshControlLabels();
        });
        this.selfListenButton.setTooltip(Tooltip.create(RaufixI18n.tr("raufixvc.studio.self_listen.desc")));
        addRenderableWidget(this.selfListenButton);
        x += listenW + gap;

        this.strengthSlider = new StrengthSlider(x, y, sliderW, 20,
                this.addon::getStrength, this.addon::setStrength);
        addRenderableWidget(this.strengthSlider);
        x += sliderW + gap + (meterReserve > 0 ? meterReserve + gap : 0);

        NeonButton doneBtn = new NeonButton(this.gridLeft + this.gridWidth - doneW, y, doneW, 20,
                RaufixI18n.tr("raufixvc.ui.done"), btn -> onClose())
                .style(NeonButton.Style.RGB_PRIMARY);
        addRenderableWidget(doneBtn);
    }

    private void refreshControlLabels() {
        if (this.enableButton != null) {
            boolean on = this.addon.isEffectEnabled();
            this.enableButton.setMessage(RaufixI18n.tr(on ? "raufixvc.picker.power.on" : "raufixvc.picker.power.off"));
            this.enableButton.style(on ? NeonButton.Style.ACTIVE_ON : NeonButton.Style.DEFAULT);
            this.enableButton.active = this.addon.isAllowedByServer();
        }
        if (this.selfListenButton != null) {
            boolean listen = this.addon.isSelfListenEnabled();
            this.selfListenButton.setMessage(RaufixI18n.tr(
                    listen ? "raufixvc.picker.listen.on" : "raufixvc.picker.listen.off"));
            this.selfListenButton.style(listen ? NeonButton.Style.ACTIVE_ON : NeonButton.Style.DEFAULT);
        }
        if (this.closeOnPickButton != null) {
            boolean autoClose = this.addon.uiConfig() != null && this.addon.uiConfig().closePickerOnSelect();
            this.closeOnPickButton.setMessage(RaufixI18n.tr(
                    autoClose ? "raufixvc.picker.auto_close.on" : "raufixvc.picker.auto_close.off"));
            this.closeOnPickButton.style(autoClose ? NeonButton.Style.ACTIVE_ON : NeonButton.Style.DEFAULT);
        }
        if (this.strengthSlider != null) {
            this.strengthSlider.refresh();
        }
    }

    @Override
    public void tick() {
        super.tick();
        refreshControlLabels();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.cardScroll.scrollBy(-(int) Math.round(verticalAmount * (CARD_HEIGHT + CARD_GAP_Y)))) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected boolean onKeyPressed(int keyCode) {
        int step = switch (keyCode) {
            case KEY_PAGE_UP -> -this.cardScroll.viewportHeight();
            case KEY_PAGE_DOWN -> this.cardScroll.viewportHeight();
            default -> 0;
        };
        return step != 0 && this.cardScroll.scrollBy(step);
    }

    @Override
    protected void paintBehindWidgets(StudioGraphics graphics) {
        graphics.drawCyberBackdrop(this.width, this.height);
        graphics.fillGradient(0, this.listTopY, this.width, this.listBottomY, 0x7A050710, 0x94070916);
    }

    @Override
    protected void paintAboveWidgets(StudioGraphics graphics, int mouseX, int mouseY) {
        StudioFrame.paintChrome(graphics, this.width, this.height, RaufixI18n.tr("raufixvc.picker.title"));
        graphics.drawRgbGradientBar(this.gridLeft, this.listTopY - 2, this.gridWidth, 1, 0.55f, 0.2f);
        graphics.drawRgbGradientBar(0, this.listBottomY, this.width, 2, 0.85f, 0.45f);

        this.cardScroll.renderScrollbar(graphics, this.gridLeft + this.gridWidth - 4);

        if (this.visibleCardCount == 0) {
            graphics.centeredText(
                    RaufixI18n.tr("raufixvc.picker.empty"),
                    this.width / 2,
                    (this.listTopY + this.listBottomY) / 2 - 4,
                    RaufixTheme.TEXT_MUTED
            );
        }

        // Mini RGB Live Spectrum Visualizer in footer when wide enough
        if (this.gridWidth >= 440) {
            int footerY = this.height - PICKER_FOOTER_HEIGHT + (PICKER_FOOTER_HEIGHT - 20) / 2;
            int doneW = 68;
            int specX = this.gridLeft + this.gridWidth - doneW - 6 - 74;
            VoiceDiagnostics diag = this.addon.getDiagnostics();
            graphics.drawSpectrumVisualizer(specX, footerY, 74, 20, diag,
                    this.addon.getInputLevel(), this.addon.isEffectActive() || this.addon.isSelfListenEnabled());
        }
    }

    @Override
    public void onClose() {
        this.addon.setSelfListenEnabled(false);
        ClientScreens.open(this.parent);
    }

    private interface BoolQuery {
        boolean get();
    }

    private record CardSpec(
            Component title,
            Component subtitle,
            int accentColor,
            boolean isNew,
            boolean allowed,
            BoolQuery selected,
            Runnable onPick
    ) {
    }
}
