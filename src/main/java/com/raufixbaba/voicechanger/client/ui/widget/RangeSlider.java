package com.raufixbaba.voicechanger.client.ui.widget;

import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Animated RGB Neon integer range slider for AI & Studio settings.
 */
public final class RangeSlider extends AbstractSliderButton {
    private final String labelKey;
    private final int min;
    private final int max;
    private final IntSupplier valueSupplier;
    private final IntConsumer valueConsumer;
    private final IntFunction<String> formatter;
    private boolean valueOnly;
    private boolean dragging;

    public RangeSlider(int x, int y, int width, int height, String labelKey, int min, int max,
                       IntFunction<String> formatter, IntSupplier valueSupplier, IntConsumer valueConsumer) {
        super(x, y, width, height, RaufixI18n.tr(labelKey), 0.0D);
        this.labelKey = labelKey;
        this.min = min;
        this.max = max;
        this.formatter = formatter;
        this.valueSupplier = valueSupplier;
        this.valueConsumer = valueConsumer;
        setTooltip(Tooltip.create(RaufixI18n.tr(labelKey + ".desc")));
        forceRefresh();
    }

    public void refresh() {
        if (this.dragging) {
            return;
        }
        forceRefresh();
    }

    private void forceRefresh() {
        int current = Math.max(this.min, Math.min(this.max, this.valueSupplier.getAsInt()));
        this.value = (current - this.min) / (double) (this.max - this.min);
        updateMessage();
    }

    public RangeSlider valueOnly() {
        this.valueOnly = true;
        updateMessage();
        return this;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.dragging = true;
        super.onClick(event, doubleClick);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        this.dragging = true;
        super.onDrag(event, dragX, dragY);
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        this.dragging = false;
        super.onRelease(event);
    }

    @Override
    protected void updateMessage() {
        String value = this.formatter.apply(currentValue());
        setMessage(this.valueOnly
                ? Component.literal(value)
                : RaufixI18n.tr(this.labelKey).copy().append(": " + value));
    }

    @Override
    protected void applyValue() {
        this.valueConsumer.accept(currentValue());
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        RgbSliderPainter.paint(g, getX(), getY(), getWidth(), getHeight(), this.value,
                this.active, isHoveredOrFocused(), getMessage());
    }

    private int currentValue() {
        return this.min + (int) Math.round(this.value * (this.max - this.min));
    }
}
