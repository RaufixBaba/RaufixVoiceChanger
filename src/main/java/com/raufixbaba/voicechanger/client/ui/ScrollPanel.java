package com.raufixbaba.voicechanger.client.ui;

import com.raufixbaba.voicechanger.client.theme.RaufixTheme;
import com.raufixbaba.voicechanger.client.ui.compat.StudioGraphics;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;

/**
 * Vertical scrolling container with animated RGB neon scrollbar thumb,
 * touch/mouse draggable scrollbar track, and mobile-safe visibility bounds.
 */
public final class ScrollPanel {
    private static final int SCROLLBAR_WIDTH = 5;
    private static final int SCROLLBAR_HIT_PADDING = 6;
    private static final int MINIMUM_THUMB_HEIGHT = 22;
    private static final int TRACK_COLOR = 0xAA080A14;

    private final List<Entry> entries = new ArrayList<>();
    private int top;
    private int bottom;
    private int lastScrollbarX = -1;
    private int scrollOffset;
    private int maxScroll;
    private boolean draggingScrollbar;

    public void clear() {
        this.entries.clear();
        this.maxScroll = 0;
        this.draggingScrollbar = false;
    }

    public <T extends AbstractWidget> T add(T widget) {
        this.entries.add(new Entry(widget, widget.getY()));
        return widget;
    }

    public void layout(int top, int bottom) {
        this.top = top;
        this.bottom = Math.max(top + 24, bottom);

        int contentEnd = top;
        for (Entry entry : this.entries) {
            contentEnd = Math.max(contentEnd, entry.baseY + entry.widget.getHeight() + 4);
        }

        this.maxScroll = Math.max(0, contentEnd - this.bottom);
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

    /**
     * Handles mouse/touch clicks on the scrollbar track so mobile / MojoLauncher
     * touchscreen players without a mouse scroll wheel can drag or tap to scroll.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || this.maxScroll <= 0 || this.lastScrollbarX < 0) {
            return false;
        }
        if (mouseX >= this.lastScrollbarX - SCROLLBAR_HIT_PADDING
                && mouseX <= this.lastScrollbarX + SCROLLBAR_WIDTH + SCROLLBAR_HIT_PADDING
                && mouseY >= this.top
                && mouseY <= this.bottom) {
            this.draggingScrollbar = true;
            scrollToMouseY(mouseY);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragY) {
        if (this.draggingScrollbar && this.maxScroll > 0) {
            scrollToMouseY(mouseY);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }
        return false;
    }

    private void scrollToMouseY(double mouseY) {
        int trackHeight = viewportHeight();
        int thumbHeight = computeThumbHeight(trackHeight);
        int thumbTravel = Math.max(1, trackHeight - thumbHeight);
        double ratio = (mouseY - this.top - thumbHeight / 2.0D) / thumbTravel;
        setScrollOffset((int) Math.round(ratio * this.maxScroll));
    }

    private int computeThumbHeight(int trackHeight) {
        return Math.max(MINIMUM_THUMB_HEIGHT,
                Math.min(trackHeight, trackHeight * trackHeight / Math.max(1, trackHeight + this.maxScroll)));
    }

    public void renderScrollbar(StudioGraphics graphics, int rightEdge) {
        this.lastScrollbarX = rightEdge;
        if (this.maxScroll <= 0) {
            return;
        }

        int trackHeight = viewportHeight();
        int thumbHeight = computeThumbHeight(trackHeight);
        int thumbTravel = Math.max(1, trackHeight - thumbHeight);
        int thumbY = this.top + this.scrollOffset * thumbTravel / this.maxScroll;

        graphics.fill(rightEdge, this.top, rightEdge + SCROLLBAR_WIDTH, this.bottom, TRACK_COLOR);
        graphics.gui().renderOutline(rightEdge, this.top, SCROLLBAR_WIDTH, trackHeight, 0x35FFFFFF);
        int cTop = RaufixTheme.rgbAt(0.0f, 0.98f);
        int cBot = RaufixTheme.rgbAt(0.45f, 0.98f);
        graphics.fillGradient(rightEdge, thumbY, rightEdge + SCROLLBAR_WIDTH, thumbY + thumbHeight, cTop, cBot);
    }

    private void apply() {
        for (Entry entry : this.entries) {
            AbstractWidget widget = entry.widget;
            int y = entry.baseY - this.scrollOffset;
            widget.setY(y);

            // Tolerant visibility check so widgets on compact/mobile viewports never all vanish mid-scroll
            int h = widget.getHeight();
            int slack = Math.min(8, h / 3);
            boolean inView = (y >= this.top - slack && y + h <= this.bottom + slack)
                    || (this.bottom - this.top < h + 8 && y + h / 2 >= this.top && y + h / 2 <= this.bottom);
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
