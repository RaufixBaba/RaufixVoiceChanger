package com.raufixbaba.voicechanger.client.ui;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.VoiceChangerController;
import com.raufixbaba.voicechanger.client.i18n.ModLanguage;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import com.raufixbaba.voicechanger.client.theme.ThemePalette;
import com.raufixbaba.voicechanger.client.ui.compat.StudioGraphics;
import com.raufixbaba.voicechanger.client.ui.widget.NeonButton;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * Animated Cyberpunk RGB Studio Chrome shared by all Raufix Voice Changer V1.0 BETA screens.
 */
public final class StudioFrame {
    public static final int HEADER_HEIGHT = 60;
    public static final int FOOTER_HEIGHT = 34;
    public static final int MARGIN = 14;
    public static final int WIDGET_HEIGHT = 20;

    private static final int TITLE_Y = 12;
    private static final int TABS_Y = 35;
    private static final int TAB_PADDING = 18;
    private static final int TAB_GAP = 4;

    public record Tab(Component name, String descriptionKey, boolean current, boolean enabled, Runnable open) {
    }

    private StudioFrame() {
    }

    public static int listTop() {
        return HEADER_HEIGHT;
    }

    public static int listBottom(int screenHeight) {
        return Math.max(HEADER_HEIGHT + 24, screenHeight - FOOTER_HEIGHT);
    }

    public static int footerY(int screenHeight) {
        return screenHeight - FOOTER_HEIGHT + (FOOTER_HEIGHT - WIDGET_HEIGHT) / 2;
    }

    public static void addTabs(List<Tab> tabs, ToIntFunction<Component> textWidth, Consumer<AbstractWidget> addWidget) {
        int x = MARGIN;
        for (Tab tab : tabs) {
            int width = textWidth.applyAsInt(tab.name()) + TAB_PADDING;
            NeonButton button = new NeonButton(x, TABS_Y, width, WIDGET_HEIGHT, tab.name(), ignored -> {
                if (!tab.current()) {
                    tab.open().run();
                }
            });
            if (tab.current()) {
                button.style(NeonButton.Style.TAB_SELECTED);
                button.active = false;
            } else {
                button.active = tab.enabled();
            }
            if (tab.descriptionKey() != null) {
                button.setTooltip(Tooltip.create(RaufixI18n.tr(tab.descriptionKey())));
            }
            addWidget.accept(button);
            x += width + TAB_GAP;
        }
    }

    /** Adds quick top-right Language (TR/EN) and RGB Palette switcher pills. */
    public static void addHeaderQuickControls(int screenWidth, VoiceChangerController addon,
                                              Runnable onRebuild, Consumer<AbstractWidget> addWidget) {
        int right = screenWidth - MARGIN;

        // Palette switcher button
        Component themeLabel = Component.literal("✦ " + RaufixI18n.tr(RaufixTheme.palette().translationKey()).getString());
        int themeW = 92;
        NeonButton themeBtn = new NeonButton(right - themeW, 8, themeW, 18, themeLabel, btn -> {
            ThemePalette next = RaufixTheme.cyclePalette();
            if (addon != null && addon.uiConfig() != null) {
                addon.uiConfig().setPalette(next);
            }
            onRebuild.run();
        }).style(NeonButton.Style.RGB_PRIMARY);
        themeBtn.setTooltip(Tooltip.create(RaufixI18n.tr("raufixvc.ui.theme.tooltip")));
        addWidget.accept(themeBtn);
        right -= themeW + 5;

        // Language switcher button (TR / EN / AUTO)
        ModLanguage lang = RaufixI18n.languageOverride();
        Component langLabel = Component.literal("🌐 " + lang.badge());
        int langW = 54;
        NeonButton langBtn = new NeonButton(right - langW, 8, langW, 18, langLabel, btn -> {
            ModLanguage next = RaufixI18n.languageOverride().next();
            RaufixI18n.setLanguageOverride(next);
            if (addon != null && addon.uiConfig() != null) {
                addon.uiConfig().setLanguage(next);
            }
            onRebuild.run();
        });
        langBtn.setTooltip(Tooltip.create(RaufixI18n.tr("raufixvc.ui.lang.tooltip")));
        addWidget.accept(langBtn);
    }

    public static void paintBackground(StudioGraphics graphics, int width, int height) {
        paintBackground(graphics, width, height, listTop());
    }

    public static void paintBackground(StudioGraphics graphics, int width, int height, int top) {
        graphics.drawCyberBackdrop(width, height);
        int bottom = listBottom(height);
        graphics.fillGradient(0, top, width, bottom, 0x7005070F, 0x88080A16);
    }

    public static void paintChrome(StudioGraphics graphics, int width, int height, Component title) {
        paintChrome(graphics, width, height, title, listTop());
    }

    public static void paintChrome(StudioGraphics graphics, int width, int height, Component title, int top) {
        // Brand title + V1 BETA pill + author credit
        int x = MARGIN;
        int titleColor = RaufixTheme.rgbAt(0.0f, 1.0f);
        graphics.text(title, x, TITLE_Y, titleColor);
        x += graphics.textWidth(title) + 8;

        int badgeW = graphics.drawPillBadge(
                Component.literal(RaufixVoiceChanger.MOD_BADGE),
                x, TITLE_Y - 2,
                0xDD171230,
                RaufixTheme.rgbAt(0.25f, 0.95f),
                0xFFFFFFFF
        );
        x += badgeW + 8;

        Component byline = Component.literal("by " + RaufixVoiceChanger.MOD_AUTHOR);
        if (x + graphics.textWidth(byline) < width - 165) {
            graphics.text(byline, x, TITLE_Y, RaufixTheme.TEXT_MUTED);
        }

        // Animated RGB neon divider lines above and below content viewport
        graphics.drawRgbGradientBar(0, top - 2, width, 2, 0.85f, 0.0f);
        int bottom = listBottom(height);
        graphics.drawRgbGradientBar(0, bottom, width, 2, 0.85f, 0.45f);
    }
}
