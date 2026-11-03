package com.raufixbaba.voicechanger.client.ui.compat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Screen base class for Raufix Voice Studio screens, exposing clean render,
 * keyboard, and touch/mouse drag hooks on Minecraft 1.21.11.
 */
public abstract class CompatScreen extends Screen {
    protected CompatScreen(Component title) {
        super(title);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        StudioGraphics graphics = new StudioGraphics(context, this.font);
        paintBehindWidgets(graphics);
        super.render(context, mouseX, mouseY, delta);
        paintAboveWidgets(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return onKeyPressed(event.key()) || super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return onScrollBarClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return onScrollBarDragged(event.x(), event.y(), event.button(), dragY)
                || super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return onScrollBarReleased(event.x(), event.y(), event.button()) || super.mouseReleased(event);
    }

    protected void paintBehindWidgets(StudioGraphics graphics) {
    }

    protected void paintAboveWidgets(StudioGraphics graphics, int mouseX, int mouseY) {
    }

    protected boolean onKeyPressed(int keyCode) {
        return false;
    }

    protected boolean onScrollBarClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    protected boolean onScrollBarDragged(double mouseX, double mouseY, int button, double dragY) {
        return false;
    }

    protected boolean onScrollBarReleased(double mouseX, double mouseY, int button) {
        return false;
    }
}
