package com.raufixbaba.voicechanger.client.ui.widget;

import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import com.raufixbaba.voicechanger.client.model.VoiceParameter;
import com.raufixbaba.voicechanger.client.model.VoiceProfile;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * Animated RGB Neon Slider bound to one {@link VoiceParameter}.
 */
public final class ParameterSlider extends AbstractSliderButton {
    private final VoiceParameter parameter;
    private final Supplier<VoiceProfile> profileSupplier;
    private final Consumer<VoiceProfile> profileConsumer;

    public ParameterSlider(
            int x,
            int y,
            int width,
            int height,
            VoiceParameter parameter,
            Supplier<VoiceProfile> profileSupplier,
            Consumer<VoiceProfile> profileConsumer
    ) {
        super(x, y, width, height, RaufixI18n.tr(parameter.translationKey()), 0.0D);
        this.parameter = parameter;
        this.profileSupplier = profileSupplier;
        this.profileConsumer = profileConsumer;
        setTooltip(Tooltip.create(RaufixI18n.tr(parameter.descriptionKey())));
        refresh(profileSupplier.get());
    }

    public void refresh(VoiceProfile profile) {
        this.value = toSliderPosition(profile.get(this.parameter));
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        Component label = RaufixI18n.tr(this.parameter.translationKey());
        setMessage(Component.literal(label.getString() + ": " + this.parameter.formatValue(currentValue())));
    }

    @Override
    protected void applyValue() {
        this.profileConsumer.accept(this.profileSupplier.get().with(this.parameter, currentValue()));
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        RgbSliderPainter.paint(g, getX(), getY(), getWidth(), getHeight(), this.value,
                this.active, isHoveredOrFocused(), getMessage());
    }

    private double currentValue() {
        double range = this.parameter.max() - this.parameter.min();
        return this.parameter.sanitize(this.parameter.min() + range * this.value);
    }

    private double toSliderPosition(double value) {
        double range = this.parameter.max() - this.parameter.min();
        if (range <= 0.0D) {
            return 0.0D;
        }
        double position = (value - this.parameter.min()) / range;
        return Math.max(0.0D, Math.min(1.0D, position));
    }
}
