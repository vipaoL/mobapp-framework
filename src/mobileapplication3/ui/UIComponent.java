// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public abstract class UIComponent implements IUIComponent {

    protected boolean isVisible = true;
    protected boolean isFocused = false;
    protected boolean isActive = true;
    public int x0, y0, w, h, prevX0, prevY0, prevW, prevH,
            anchorX0, anchorY0,
            anchor = IUIComponent.LEFT | IUIComponent.TOP;
    private boolean isSizeSet = false;
    protected int bgColor = COLOR_TRANSPARENT;
    private boolean roundBg = true;
    protected int padding;
    private IContainer parent = null;

    public final void paint(Graphics g) {
        paint(g, false);
    }

    public final void paint(Graphics g, boolean forceInactive) {
        paint(g, x0, y0, w, h, forceInactive);
    }

    public final void paint(Graphics g, int x0, int y0, int w, int h) {
        paint(g, x0, y0, w, h, false);
    }

    public final void paint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (isVisible) {
            int prevClipX = g.getClipX();
            int prevClipY = g.getClipY();
            int prevClipW = g.getClipWidth();
            int prevClipH = g.getClipHeight();

//            x0 += padding;
//            y0 += padding;
//            w -= padding*2;
//            h -= padding*2;

            if (w <= 0 || h <= 0) {
                return;
            }

            int clipX = x0 + padding, clipY = y0 + padding, clipW = w - padding * 2, clipH = h - padding * 2, clipX2 = clipX + clipW, clipY2 = clipY + clipH;
            clipX = Math.max(clipX, prevClipX);
            clipY = Math.max(clipY, prevClipY);
            clipX2 = Math.min(clipX2, prevClipX + prevClipW);
            clipY2 = Math.min(clipY2, prevClipY + prevClipH);
            clipW = Math.max(0, clipX2 - clipX);
            clipH = Math.max(0, clipY2 - clipY);
            g.setClip(clipX, clipY, clipW, clipH);

            setBounds(x0, y0, w, h);
            drawBg(g, x0 + padding, y0 + padding, w - padding*2, h - padding*2, isActive && !forceInactive);
            onPaint(g, x0 + padding, y0 + padding, w - padding*2, h - padding*2, forceInactive);

            g.setClip(prevClipX, prevClipY, prevClipW, prevClipH);
        }
    }

    public void drawBg(Graphics g, int x0, int y0, int w, int h, boolean IsActive) {
        int bgColor;
        if (isActive) {
            bgColor = this.bgColor;
        } else {
            bgColor = BG_COLOR_INACTIVE;
        }

        if (bgColor >= 0) {
            g.setColor(bgColor);
            if (roundBg) {
                int d = Math.min(w/5, h/5);
                g.fillRoundRect(x0, y0, w, h, d, d);
            } else {
                g.fillRect(x0, y0, w, h);
            }
        }
    }

    public boolean checkTouchEvent(int x, int y) {
        if (!isActive || !isVisible) {
            return false;
        }

        if (x < x0 || y < y0) {
            return false;
        }

        if (x - x0 > w || y - y0 > h) {
            return false;
        }

        return true;
    }

    public IUIComponent setVisible(boolean b) {
        isVisible = b;
        return this;
    }

    public IUIComponent setFocused(boolean b) {
        isFocused = b;
        return this;
    }

    public IUIComponent setActive(boolean b) {
        isActive = b;
        return this;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public boolean repaintOnlyOnFlushGraphics() {
        return false;
    }

    public boolean toggleIsVisible() {
        setVisible(!isVisible);
        return isVisible;
    }

    public void recalcSize() {
        setSize(w, h);
    }

    public void recalcPos() {
        setPos(anchorX0, anchorY0, anchor);
    }

//    public IUIComponent setPos(int x0, int y0) {
//        this.x0 = x0;
//        this.y0 = y0;
//        anchorX0 = x0;
//        anchorY0 = y0;
//        return this;
//    }

    public IUIComponent setPos(int x0, int y0, int anchor) {
        anchorX0 = x0;
        anchorY0 = y0;
        this.anchor = anchor;

        if ((anchor & IUIComponent.RIGHT) != 0) {
            x0 -= w;
        } else if ((anchor & IUIComponent.HCENTER) != 0) {
            x0 -= w/2;
        }
        if ((anchor & IUIComponent.BOTTOM) != 0) {
            y0 -= h;
        } else if ((anchor & IUIComponent.VCENTER) != 0) {
            y0 -= h/2;
        }

        setBounds(x0, y0, w, h);

        return this;
    }

    public IUIComponent setSize(int w, int h) {
        if (w == 0 || h == 0) {
            try {
                throw new Exception("Setting zero as a dimension " + getClass().getName());
            } catch (Exception ex) {
                Logger.log(ex);
            }
            return this;
        }

        this.w = w;
        this.h = h;

        recalcPos();
        return this;
    }

    private final void setBounds(int x0, int y0, int w, int h) {
        if (x0 == prevX0 && y0 == prevY0 && w == prevW && h == prevH) {
            return;
        }

        prevW = this.w;
        prevH = this.h;
        prevX0 = this.x0;
        prevY0 = this.y0;

        this.w = w;
        this.h = h;
        this.x0 = x0;
        this.y0 = y0;

        onSetBounds(x0, y0, w, h);
        isSizeSet = true;
    }

    protected void onSetBounds(int x0, int y0, int w, int h) { }

    public IUIComponent setBgColor(int color) {
        bgColor = color;
        return this;
    }

    public IUIComponent setPadding(int padding) {
        this.padding = padding;
        return this;
    }

    public IUIComponent roundBg(boolean b) {
        roundBg = b;
        return this;
    }

    public int getLeftX() {
        return x0;
    }

    public int getRightX() {
        return x0 + w;
    }

    public int getTopY() {
        return y0;
    }

    public int getBottomY() {
        return y0 + h;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public boolean isSizeSet() {
        return isSizeSet;
    }

    public void init() { }

    public void postInit() { }

    public IUIComponent setParent(IContainer parent) {
        this.parent = parent;
        return this;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public UISettings getUISettings() {
        if (hasParent()) {
            return parent.getUISettings();
        } else {
            try {
                throw new IllegalStateException(getClass().getName() + " has no parent and can't get UI settings");
            } catch (IllegalStateException ex) {
                Logger.log(ex);
            }
            return null;
        }
    }

    public void repaint() {
        if (parent != null) {
            parent.repaint();
        } else {
            try {
                throw new NullPointerException("Can't call parent's repaint: parent component is not set! " + getClass().getName());
            } catch (NullPointerException ex) {
                Logger.log(ex);
            }
        }
    }

    public Graphics getUGraphics() {
        if (parent != null) {
            return parent.getUGraphics();
        } else {
            try {
                throw new NullPointerException("Can't call parent's getGraphics: parent component is not set! " + getClass().getName());
            } catch (NullPointerException ex) {
                Logger.log(ex);
                return null;
            }
        }
    }

    public void flushGraphics() {
        if (parent != null) {
            parent.flushGraphics();
        } else {
            try {
                throw new NullPointerException("Can't call parent's getGraphics: parent component is not set! " + getClass().getName());
            } catch (NullPointerException ex) {
                Logger.log(ex);
            }
        }
    }

    public boolean isOnScreen() {
        return hasParent() && parent.isOnScreen();
    }

    public void onShow() { }
    public void onHide() { }

    public boolean pointerReleased(int x, int y) {
        if (!isVisible) {
            return false;
        }
        return handlePointerReleased(x, y);
    }

    public boolean pointerClicked(int x, int y) {
        if (!(isVisible && checkTouchEvent(x, y))) {
            return false;
        }
        return handlePointerClicked(x, y);
    }

    public boolean pointerDragged(int x, int y) {
        if (!isVisible) {
            return false;
        }
        return handlePointerDragged(x, y);
    }

    public boolean pointerPressed(int x, int y) {
        if (!(isVisible && checkTouchEvent(x, y))) {
            return false;
        }
        return handlePointerPressed(x, y);
    }

    public boolean keyPressed(int keyCode, int count) {
        if (!isActive || !isVisible) {
            return false;
        }
        return handleKeyPressed(keyCode, count);
    }

    public boolean keyRepeated(int keyCode, int pressedCount) {
        if (!isActive || !isVisible) {
            return false;
        }
        return handleKeyRepeated(keyCode, pressedCount);
    }

    public boolean keyReleased(int keyCode, int count) {
        if (!isActive || !isVisible) {
            return false;
        }
        return handleKeyReleased(keyCode, count);
    }

    public boolean mouseEvent(int event, int x, int y) {
        if (!isActive || !isVisible) {
            return false;
        }
        return handleMouseEvent(event, x, y);
    }

    protected boolean handleMouseEvent(int event, int x, int y) {
        return false;
    }

    protected boolean handleKeyReleased(int keyCode, int pressedCount) {
        return false;
    }

    protected boolean handleKeyRepeated(int keyCode, int pressedCount) {
        return false;
    }

    protected boolean handlePointerReleased(int x, int y) {
        return false;
    }

    protected boolean handlePointerDragged(int x, int y) {
        return false;
    }

    protected boolean handlePointerPressed(int x, int y) {
        return false;
    }

    protected abstract boolean handlePointerClicked(int x, int y);
    protected abstract boolean handleKeyPressed(int keyCode, int count);
    protected abstract void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive);

}
