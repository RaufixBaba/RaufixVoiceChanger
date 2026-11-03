package com.raufixbaba.voicechanger.client.ui;

import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import com.raufixbaba.voicechanger.client.ui.compat.StudioGraphics;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;

/**
 * Vertical scrolling container with animated RGB neon scrollbar thumb.
 */
public final class ScrollPanel {
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int MINIMUM_THUMB_HEIGHT = 20;
    private static final int TRACK_COLOR = 0x88080A14;

    private final List<Entry> entries = new ArrayList<>();
    private int top;
    private int bottom;
    private int scrollOffset;
    private int maxScroll;

    public void clear() {
        this.entries.clear();
        this.maxScroll = 0;
    }

    public <T extends AbstractWidget> T add(T widget) {
        this.entries.add(new Entry(widget, widget.getY()));
        return widget;
    }

    public void layout(int top, int bottom) {
        this.top = top;
        this.bottom = bottom;

        int contentEnd = top;
        for (Entry entry : this.entries) {
            contentEnd = Math.max(contentEnd, entry.baseY + entry.widget.getHeight());
        }

        this.maxScroll = Math.max(0, contentEnd - bottom);
        this.scrollOffset = clamp(this.scrollOffset, 0, this.maxScroll);
        apply();
    }

    public boolean isScrollable() {
        return this.maxScroll > 0;
    }

    public boolean scrollBy(int pixels) {
        if (this.maxScroll <= 0) {
            return false;
        }

        int updated = clamp(this.scrollOffset + pixels, 0, this.maxScroll);
        if (updated == this.scrollOffset) {
            return true;
        }

        this.scrollOffset = updated;
        apply();
        return true;
    }

    public int scrollOffset() {
        return this.scrollOffset;
    }

    public void setScrollOffset(int offset) {
        this.scrollOffset = clamp(offset, 0, this.maxScroll);
        apply();
    }

    public int viewportHeight() {
        return Math.max(1, this.bottom - this.top);
    }

    public void renderScrollbar(StudioGraphics graphics, int rightEdge) {
        if (this.maxScroll <= 0) {
            return;
        }

        int trackHeight = viewportHeight();
        int thumbHeight = Math.max(MINIMUM_THUMB_HEIGHT, trackHeight * trackHeight / (trackHeight + this.maxScroll));
        int thumbTravel = Math.max(1, trackHeight - thumbHeight);
        int thumbY = this.top + this.scrollOffset * thumbTravel / this.maxScroll;

        graphics.fill(rightEdge, this.top, rightEdge + SCROLLBAR_WIDTH, this.bottom, TRACK_COLOR);
        int cTop = RaufixTheme.rgbAt(0.0f, 0.95f);
        int cBot = RaufixTheme.rgbAt(0.45f, 0.95f);
        graphics.fillGradient(rightEdge, thumbY, rightEdge + SCROLLBAR_WIDTH, thumbY + thumbHeight, cTop, cBot);
    }

    private void apply() {
        for (Entry entry : this.entries) {
            AbstractWidget widget = entry.widget;
            int y = entry.baseY - this.scrollOffset;
            widget.setY(y);

            boolean inView = y >= this.top && y + widget.getHeight() <= this.bottom;
            if (!inView && !entry.hiddenByScroll) {
                entry.hiddenByScroll = true;
                entry.wasVisible = widget.visible;
                entry.wasActive = widget.active;
                widget.visible = false;
                widget.active = false;
            } else if (inView && entry.hiddenByScroll) {
                entry.hiddenByScroll = false;
                widget.visible = entry.wasVisible;
                widget.active = entry.wasActive;
            }
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class Entry {
        final AbstractWidget widget;
        final int baseY;
        boolean hiddenByScroll;
        boolean wasVisible;
        boolean wasActive;

        Entry(AbstractWidget widget, int baseY) {
            this.widget = widget;
            this.baseY = baseY;
        }
    }
}
