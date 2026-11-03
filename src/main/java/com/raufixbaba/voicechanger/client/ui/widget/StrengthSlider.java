package com.raufixbaba.voicechanger.client.ui.widget;

import com.raufixbaba.voicechanger.client.MinecraftTextAccess;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * Animated RGB Neon Strength Slider (%0 - %100).
 */
public final class StrengthSlider extends AbstractSliderButton {
    private static final String LABEL_KEY = "raufixvc.slider.strength";

    private final IntSupplier valueSupplier;
    private final IntConsumer valueConsumer;
    private boolean valueOnly;

    public StrengthSlider(int x, int y, int width, int height, IntSupplier valueSupplier, IntConsumer valueConsumer) {
        super(x, y, width, height, RaufixI18n.tr(LABEL_KEY), 0.0D);
        this.valueSupplier = valueSupplier;
        this.valueConsumer = valueConsumer;
        setTooltip(Tooltip.create(RaufixI18n.tr(LABEL_KEY + ".desc")));
        refresh();
    }

    public void refresh() {
        this.value = Math.max(0.0D, Math.min(1.0D, this.valueSupplier.getAsInt() / 100.0D));
        updateMessage();
    }

    public StrengthSlider valueOnly() {
        this.valueOnly = true;
        updateMessage();
        return this;
    }

    @Override
    protected void updateMessage() {
        setMessage(this.valueOnly
                ? Component.literal("%" + percent())
                : RaufixI18n.tr(LABEL_KEY).copy().append(": %" + percent()));
    }

    @Override
    protected void applyValue() {
        this.valueConsumer.accept(percent());
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        RgbSliderPainter.paint(g, getX(), getY(), getWidth(), getHeight(), this.value,
                this.active, isHoveredOrFocused(), getMessage());
    }

    private int percent() {
        return (int) Math.round(this.value * 100.0D);
    }
}
