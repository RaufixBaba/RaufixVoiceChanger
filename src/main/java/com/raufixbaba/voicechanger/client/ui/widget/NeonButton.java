package com.raufixbaba.voicechanger.client.ui.widget;

import com.raufixbaba.voicechanger.client.MinecraftTextAccess;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/**
 * Animated RGB Cyber-Glass button with smooth hover transitions and multiple
 * studio visual styles.
 */
public final class NeonButton extends AbstractButton {
    public enum Style {
        DEFAULT,
        RGB_PRIMARY,
        ACTIVE_ON,
        TAB_SELECTED,
        DANGER
    }

    private final Consumer<NeonButton> onPressAction;
    private Style style = Style.DEFAULT;
    private float hoverAnim;
    private float phaseSeed;

    public NeonButton(int x, int y, int width, int height, Component message, Consumer<NeonButton> onPressAction) {
        super(x, y, width, height, message);
        this.onPressAction = onPressAction;
        this.phaseSeed = ((x * 31 + y * 17) & 0xFF) / 255.0f;
    }

    public NeonButton style(Style style) {
        this.style = style;
        return this;
    }

    public Style style() {
        return this.style;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (this.active && this.onPressAction != null) {
            this.onPressAction.accept(this);
        }
    }

    @Override
    protected void renderContents(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.active && (this.isHoveredOrFocused());
        float target = hovered ? 1.0f : 0.0f;
        this.hoverAnim += (target - this.hoverAnim) * 0.24f;

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        if (!this.active && this.style != Style.TAB_SELECTED) {
            g.fill(x, y, x + w, y + h, 0xAA0A0C14);
            g.renderOutline(x, y, w, h, 0x30FFFFFF);
            drawLabel(g, 0xFF646C82);
            return;
        }

        switch (this.style) {
            case RGB_PRIMARY -> renderRgbPrimary(g, x, y, w, h);
            case ACTIVE_ON -> renderAccent(g, x, y, w, h, RaufixTheme.SUCCESS, 0xEE0B221B, 0xF0081813);
            case DANGER -> renderAccent(g, x, y, w, h, RaufixTheme.DANGER, 0xEE260D15, 0xF019080E);
            case TAB_SELECTED -> renderTabSelected(g, x, y, w, h);
            case DEFAULT -> renderDefault(g, x, y, w, h);
        }
    }

    private void renderRgbPrimary(GuiGraphics g, int x, int y, int w, int h) {
        int cLeft = RaufixTheme.rgbAt(this.phaseSeed, 0.30f + 0.22f * this.hoverAnim);
        int cRight = RaufixTheme.rgbAt(this.phaseSeed + 0.45f, 0.24f + 0.22f * this.hoverAnim);
        g.fill(x, y, x + w, y + h, 0xF0101428);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, cLeft, cRight);

        // Multi-segment flowing RGB outline + hover glow
        int segs = Math.max(4, Math.min(18, w / 8));
        float borderAlpha = 0.82f + 0.18f * this.hoverAnim;
        for (int i = 0; i < segs; i++) {
            int sx = x + (i * w) / segs;
            int ex = x + ((i + 1) * w) / segs;
            int col = RaufixTheme.rgbAt(this.phaseSeed + i * 0.05f, borderAlpha);
            g.fill(sx, y, ex, y + 1, col);
            g.fill(sx, y + h - 1, ex, y + h, col);
        }
        g.fill(x, y, x + 1, y + h, RaufixTheme.rgbAt(this.phaseSeed, borderAlpha));
        g.fill(x + w - 1, y, x + w, y + h, RaufixTheme.rgbAt(this.phaseSeed + 0.5f, borderAlpha));

        if (this.hoverAnim > 0.05f) {
            int glow = RaufixTheme.rgbAt(this.phaseSeed + 0.2f, 0.28f * this.hoverAnim);
            g.renderOutline(x - 1, y - 1, w + 2, h + 2, glow);
        }
        drawLabel(g, 0xFFFFFFFF);
    }

    private void renderTabSelected(GuiGraphics g, int x, int y, int w, int h) {
        g.fillGradient(x, y, x + w, y + h, 0xF2181E38, 0xF20F1426);
        int segs = Math.max(4, Math.min(14, w / 6));
        for (int i = 0; i < segs; i++) {
            int sx = x + (i * w) / segs;
            int ex = x + ((i + 1) * w) / segs;
            int col = RaufixTheme.rgbAt(this.phaseSeed + i * 0.06f, 0.95f);
            g.fill(sx, y + h - 2, ex, y + h, col);
            g.fill(sx, y, ex, y + 1, RaufixTheme.withAlpha(col, 0.55f));
        }
        g.fill(x, y, x + 1, y + h, RaufixTheme.rgbAt(this.phaseSeed, 0.55f));
        g.fill(x + w - 1, y, x + w, y + h, RaufixTheme.rgbAt(this.phaseSeed + 0.4f, 0.55f));
        drawLabel(g, 0xFFFFFFFF);
    }

    private void renderAccent(GuiGraphics g, int x, int y, int w, int h, int accent, int topBg, int botBg) {
        g.fillGradient(x, y, x + w, y + h, topBg, botBg);
        if (this.hoverAnim > 0.02f) {
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, RaufixTheme.withAlpha(accent, 0.14f * this.hoverAnim));
        }
        int border = RaufixTheme.withAlpha(accent, 0.68f + 0.30f * this.hoverAnim);
        g.renderOutline(x, y, w, h, border);
        g.fill(x, y + h - 2, x + w, y + h - 1, RaufixTheme.withAlpha(accent, 0.40f));
        drawLabel(g, 0xFFF5FAFF);
    }

    private void renderDefault(GuiGraphics g, int x, int y, int w, int h) {
        int topBg = this.hoverAnim > 0.02f ? 0xEE181F36 : 0xE6101525;
        int botBg = this.hoverAnim > 0.02f ? 0xF0101628 : 0xEC0B0E1A;
        g.fillGradient(x, y, x + w, y + h, topBg, botBg);

        if (this.hoverAnim > 0.04f) {
            int hoverBorder = RaufixTheme.rgbAt(this.phaseSeed, 0.40f + 0.50f * this.hoverAnim);
            g.renderOutline(x, y, w, h, hoverBorder);
            g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, RaufixTheme.withAlpha(hoverBorder, 0.35f * this.hoverAnim));
        } else {
            g.renderOutline(x, y, w, h, 0x389AA8CC);
        }

        int textColor = this.hoverAnim > 0.25f ? 0xFFFFFFFF : 0xFFE3E9F8;
        drawLabel(g, textColor);
    }

    private void drawLabel(GuiGraphics g, int color) {
        Font font = Minecraft.getInstance().font;
        Component msg = getMessage();
        int maxW = Math.max(12, getWidth() - 10);
        if (font.width(msg) > maxW) {
            String plain = msg.getString();
            while (plain.length() > 1 && font.width(plain + "…") > maxW) {
                plain = plain.substring(0, plain.length() - 1);
            }
            msg = Component.literal(plain + "…");
        }
        int textY = getY() + (getHeight() - 8) / 2;
        MinecraftTextAccess.drawCentered(g, font, msg, getX() + getWidth() / 2, textY, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
