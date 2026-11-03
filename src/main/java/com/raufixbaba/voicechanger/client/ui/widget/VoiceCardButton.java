package com.raufixbaba.voicechanger.client.ui.widget;

import com.raufixbaba.voicechanger.client.MinecraftTextAccess;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/**
 * Interactive RGB Neon Voice Selection Card for {@code VoicePickerScreen}.
 * Displays the voice name, realistic acoustic summary, category accent bar,
 * "YENİ" badge for new V1 Beta voices, and live "✦ SEÇİLİ" active state.
 */
public final class VoiceCardButton extends AbstractButton {
    private final Component subtitle;
    private final int categoryAccent;
    private final boolean isNew;
    private final BooleanSupplier selectedSupplier;
    private final Runnable onSelect;
    private float hoverAnim;
    private final float phaseOffset;

    public VoiceCardButton(
            int x,
            int y,
            int width,
            int height,
            Component title,
            Component subtitle,
            int categoryAccent,
            boolean isNew,
            BooleanSupplier selectedSupplier,
            Runnable onSelect
    ) {
        super(x, y, width, height, title);
        this.subtitle = subtitle;
        this.categoryAccent = categoryAccent;
        this.isNew = isNew;
        this.selectedSupplier = selectedSupplier;
        this.onSelect = onSelect;
        this.phaseOffset = ((x * 19 + y * 37) & 0xFF) / 255.0f;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (this.active && this.onSelect != null) {
            this.onSelect.run();
        }
    }

    @Override
    protected void renderContents(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean selected = this.selectedSupplier.getAsBoolean();
        boolean hovered = this.active && isHoveredOrFocused();
        float target = (hovered || selected) ? 1.0f : 0.0f;
        this.hoverAnim += (target - this.hoverAnim) * 0.22f;

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Card interior
        int bgTop = selected ? 0xF217203B : (hovered ? 0xEE141B31 : 0xE60D1120);
        int bgBot = selected ? 0xF50F162B : (hovered ? 0xF00E1324 : 0xEC090C16);
        g.fillGradient(x, y, x + w, y + h, bgTop, bgBot);

        if (this.hoverAnim > 0.02f) {
            int wash = selected
                    ? RaufixTheme.rgbAt(this.phaseOffset, 0.13f)
                    : RaufixTheme.withAlpha(this.categoryAccent, 0.09f * this.hoverAnim);
            g.fill(x + 4, y + 1, x + w - 1, y + h - 1, wash);
        }

        // Left 4px glowing category/RGB bar
        int barColor = selected
                ? RaufixTheme.rgbAt(this.phaseOffset, 1.0f)
                : RaufixTheme.withAlpha(this.categoryAccent, 0.72f + 0.28f * this.hoverAnim);
        g.fill(x, y, x + 4, y + h, barColor);

        // Outline: animated RGB rainbow when selected, category neon on hover, subtle glass at rest
        if (selected) {
            int segs = Math.max(6, Math.min(16, w / 10));
            for (int i = 0; i < segs; i++) {
                int sx = x + (i * w) / segs;
                int ex = x + ((i + 1) * w) / segs;
                int col = RaufixTheme.rgbAt(this.phaseOffset + i * 0.05f, 0.95f);
                g.fill(sx, y, ex, y + 1, col);
                g.fill(sx, y + h - 1, ex, y + h, col);
            }
            g.fill(x + w - 1, y, x + w, y + h, RaufixTheme.rgbAt(this.phaseOffset + 0.5f, 0.95f));
        } else if (hovered) {
            g.renderOutline(x, y, w, h, RaufixTheme.withAlpha(this.categoryAccent, 0.85f));
        } else {
            g.renderOutline(x, y, w, h, 0x2E8FA0C8);
        }

        Font font = Minecraft.getInstance().font;
        int rightReserved = 8;

        // Badges on top-right: [SEÇİLİ] or [YENİ]
        int badgeRight = x + w - 6;
        if (selected) {
            Component activeBadge = RaufixI18n.tr("raufixvc.badge.selected");
            int bw = font.width(activeBadge) + 8;
            int bx = badgeRight - bw;
            int by = y + 5;
            g.fill(bx, by, bx + bw, by + 11, 0xDD0D2B22);
            g.renderOutline(bx, by, bw, 11, RaufixTheme.SUCCESS);
            MinecraftTextAccess.draw(g, font, activeBadge, bx + 4, by + 2, RaufixTheme.SUCCESS);
            badgeRight = bx - 4;
            rightReserved += bw + 6;
        } else if (this.isNew) {
            Component newBadge = RaufixI18n.tr("raufixvc.badge.new");
            int bw = font.width(newBadge) + 8;
            int bx = badgeRight - bw;
            int by = y + 5;
            int rgbCol = RaufixTheme.rgbAt(this.phaseOffset, 0.90f);
            g.fill(bx, by, bx + bw, by + 11, 0xCC16122E);
            g.renderOutline(bx, by, bw, 11, rgbCol);
            MinecraftTextAccess.draw(g, font, newBadge, bx + 4, by + 2, 0xFFFFFFFF);
            rightReserved += bw + 6;
        }

        // Title
        int textX = x + 10;
        int maxTitleW = Math.max(24, w - 14 - rightReserved);
        Component titleText = fitText(font, getMessage(), maxTitleW);
        int titleColor = !this.active ? 0xFF6E758A : (selected ? 0xFFFFFFFF : 0xFFF2F6FF);
        MinecraftTextAccess.draw(g, font, titleText, textX, y + 6, titleColor);

        // Subtitle description
        if (h >= 32 && this.subtitle != null) {
            int maxSubW = Math.max(24, w - 16);
            Component subText = fitText(font, this.subtitle, maxSubW);
            int subColor = selected ? 0xFFC5D4F5 : 0xFF8E9BB8;
            MinecraftTextAccess.draw(g, font, subText, textX, y + 19, subColor);
        }
    }

    private static Component fitText(Font font, Component text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String plain = text.getString();
        while (plain.length() > 1 && font.width(plain + "…") > maxWidth) {
            plain = plain.substring(0, plain.length() - 1);
        }
        return Component.literal(plain + "…");
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
