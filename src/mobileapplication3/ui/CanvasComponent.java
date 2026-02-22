// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.Image;

/**
 *
 * @author vipaol
 */
public abstract class CanvasComponent implements IContainer, IUIComponent, IPopupFeedback {

    protected int x0, y0, w, h, prevX0, prevY0, prevW, prevH,
            anchorX0, anchorY0,
            anchor = IUIComponent.LEFT | IUIComponent.TOP;
    protected boolean isVisible = true;
    protected boolean isFocused = true;
    protected boolean isActive = true;
    protected int pressedX, pressedY;
    private int bgColor = 0x000000;
    private boolean roundBg = false;
    private int padding;
    protected Image bg = null;
    private IUIComponent popupWindow = null;
    protected IContainer parent = null;
    protected boolean repaintOnlyOnFlushGraphics = false;

    public void init() { }

    public void postInit() { }

    public IUIComponent setBgColor(int bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public IUIComponent roundBg(boolean b) {
        roundBg = b;
        return this;
    }

    public IUIComponent setPadding(int padding) {
        this.padding = padding;
        return this;
    }

    public CanvasComponent setBgImage(Image bg) {
        this.bg = bg;
        return this;
    }

    public Image getCapture() {
        try {
            Image capture = Image.createImage(w, h);
            onPaint(capture.getGraphics(), true);
            return capture;
        } catch (Exception ex) {
            Logger.log(ex);
            return null;
        }
    }

    public Image getBlurredCapture() {
        Image img = getCapture();
        if (img != null) {
            img.blur();
        }
        return img;
    }

    public final void showPopup(IUIComponent w) {
        popupWindow = w;
        popupWindow.setParent(this);
        popupWindow.init();
        refreshFocusedComponents();
        popupWindow.setSize(this.w, h).setPos(x0, y0, TOP | LEFT);
        repaint();
    }

    public final void closePopup() {
        if (this.popupWindow != null) {
            popupWindow.setVisible(false);
            popupWindow.setParent(null);
        }
        this.popupWindow = null;
        refreshFocusedComponents();
        refreshSizes();
        repaint();
    }

    public boolean isPopupShown() {
        return popupWindow != null;
    }

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
        if (!isVisible) {
            return;
        }

        if (w == 0 || h == 0) {
            try {
                throw new Exception("Can't paint: w=" + w + " h=" + h + " " + getClass().getName());
            } catch (Exception ex) {
                Logger.log(ex);
            }
            return;
        }

        forceInactive = forceInactive || !isActive;

        x0 += padding;
        y0 += padding;
        w -= padding*2;
        h -= padding*2;

        if (w <= 0 || h <= 0) {
            return;
        }

        int prevClipX = g.getClipX();
        int prevClipY = g.getClipY();
        int prevClipW = g.getClipWidth();
        int prevClipH = g.getClipHeight();
        g.setClip(x0, y0, w, h);
        drawBg(g, x0, y0, w, h, forceInactive);

        onPaint(g, x0, y0, w, h, forceInactive);
        if (popupWindow != null) {
            popupWindow.paint(g, x0, y0, w, h, forceInactive);
        }

        g.setClip(prevClipX, prevClipY, prevClipW, prevClipH);
    }

    private final void onPaint(Graphics g, boolean forceInactive) {
        onPaint(g, x0, y0, w, h, forceInactive);
    }

    protected abstract void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive);

    protected void drawBg(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        int bgColor;
        if (!forceInactive) {
            bgColor = this.bgColor;
        } else {
            bgColor = BG_COLOR_INACTIVE;
        }

        if (bgColor >= 0) {
            g.setColor(bgColor);
            if (roundBg) {
                int r = Math.min(w/5, h/5);
                g.fillRoundRect(x0, y0, w, h, r, r);
            } else {
                g.fillRect(x0, y0, w, h);
            }
        }

        if (bg != null) {
            g.drawImage(bg, x0 + w/2, y0 + h/2, Graphics.VCENTER | Graphics.HCENTER);
        }
    }

    public final void refreshSizes() {
        setSize(w, h);
    }

    public abstract boolean canBeFocused();

    public boolean isFocused() {
        return isFocused;
    }

    protected final void refreshFocusedComponents() {
        if (popupWindow != null) {
            popupWindow.setFocused(true);
        }
    }

    public IUIComponent setVisible(boolean b) {
        isVisible = b;
        return this;
    }

    public IUIComponent setFocused(boolean b) {
        isFocused = b;
        refreshFocusedComponents();
        return this;
    }

    public IUIComponent setActive(boolean b) { // TODO: merge with refreshFocusedComponents
        //System.out.println(getClass().getName() + " setActive: " + b);
        isActive = b;
        return this;
    }

    public boolean toggleIsVisible() {
        setVisible(!isVisible);
        return isVisible;
    }

    public final boolean isVisible() {
        return isVisible;
    }

    public final boolean repaintOnlyOnFlushGraphics() {
        return repaintOnlyOnFlushGraphics;
    }

    public final boolean checkTouchEvent(int x, int y) {
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

    public final boolean pointerClicked(int x, int y) {
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            popupWindow.pointerClicked(x, y);
            repaint();
            if (isTarget) {
                return true;
            }
        }

        return handlePointerClicked(x, y);
    }

    protected boolean handlePointerClicked(int x, int y) {
        return false;
    };

    public final boolean pointerReleased(int x, int y) {
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            popupWindow.pointerReleased(x, y);
            repaint();
            if (isTarget) {
                return true;
            }
        }

        return handlePointerReleased(x, y);
    }

    protected boolean handlePointerReleased(int x, int y) {
        return false;
    };

    public final boolean pointerDragged(int x, int y) {
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        if (Math.abs(x - pressedX) + Math.abs(y - pressedY) < 5) {
            return false;
        }

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            popupWindow.pointerDragged(x, y);
            if (isTarget) {
                repaint();
                return true;
            }
        }

        return handlePointerDragged(x, y);
    }

    protected boolean handlePointerDragged(int x, int y) {
        return false;
    };

    public final boolean pointerPressed(int x, int y) {
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        pressedX = x;
        pressedY = y;

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            popupWindow.pointerPressed(x, y);
            if (isTarget) {
                repaint();
                return true;
            }
        }

        return handlePointerPressed(x, y);
    }

    protected boolean handlePointerPressed(int x, int y) {
        return false;
    };

    public final boolean mouseEvent(int event, int x, int y) {
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            if (isTarget) {
                return popupWindow.mouseEvent(event, x, y); // TODO
            }
        }

        return handleMouseEvent(event, x, y);
    }

    public boolean handleMouseEvent(int event, int x, int y) {
        return false;
    }

    public final boolean keyPressed(int keyCode, int count) {
        if (!isActive || !isVisible) {
            return false;
        }

        if (popupWindow != null) {
            popupWindow.keyPressed(keyCode, count);
            repaint();
            return true;
        }

        return handleKeyPressed(keyCode, count);
    }

    protected boolean handleKeyPressed(int keyCode, int count) {
        return false;
    };

    public final boolean keyReleased(int keyCode, int count) {
        if (!isActive || !isVisible) {
            return false;
        }

        if (popupWindow != null) {
            popupWindow.keyReleased(keyCode, count);
            repaint();
            return true;
        }

        return handleKeyReleased(keyCode, count);
    }

    protected boolean handleKeyReleased(int keyCode, int count) {
        return false;
    }

    public final boolean keyRepeated(int keyCode, int pressedCount) {
        if (!isActive || !isVisible) {
            return false;
        }

        if (popupWindow != null) {
            popupWindow.keyRepeated(keyCode, pressedCount);
            repaint();
            return true;
        }

        return handleKeyRepeated(keyCode, pressedCount);
    }

    protected boolean handleKeyRepeated(int keyCode, int pressedCount) {
        return false;
    };

    public void onShow() { }
    public void onHide() { }

    public final IUIComponent setPos(int x0, int y0) {
        this.x0 = x0;
        this.y0 = y0;
        return this;
    }

    public final IUIComponent setPos(int x0, int y0, int anchor) {
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

        setPos(x0, y0);
        setBounds(x0, y0, w, h);

        return this;
    }

    public final IUIComponent setSize(int w, int h) {
        if (this.w == w && this.h == h || w == 0 || h == 0) {
            return this;
        }

        setBounds(x0, y0, w, h);
        setPos(anchorX0, anchorY0, anchor);

        return this;
    }

    private final void setBounds(int x0, int y0, int w, int h) {
        if (w == 0 || h == 0) {
            try {
                throw new Exception("Setting zero as a dimension " + getClass().getName());
            } catch (Exception ex) {
                Logger.log(ex);
            }
            return;
        }
        if (x0 == prevX0 && y0 == prevY0 && w == prevW && h == prevH) {
            return;
        }

        if (popupWindow != null) {
            popupWindow.setSize(w, h).setPos(x0, y0, LEFT | TOP);
        }

        if (prevW != 0 && prevH != 0 && w != prevW && h != prevH) {
            bg = null;
            if (bgColor == COLOR_TRANSPARENT) {
                bgColor = 0;
            }
        }

        prevW = this.w = w;
        prevH = this.h = h;
        prevX0 = this.x0 = x0;
        prevY0 = this.y0 = y0;

        onSetBounds(x0, y0, w, h);
    }

    public final IUIComponent setParent(IContainer parent) {
        this.parent = parent;
        return this;
    }

    public final boolean hasParent() {
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

    public final void repaint() {
        if (!isVisible) {
            return;
        }

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

    public final Graphics getUGraphics() {
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

    public final void flushGraphics() {
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

    protected abstract void onSetBounds(int x0, int y0, int w, int h);

}