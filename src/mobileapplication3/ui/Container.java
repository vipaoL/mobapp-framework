// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.Image;

/**
 *
 * @author vipaol
 */
public abstract class Container implements IContainer, IUIComponent, IPopupFeedback {

    private IUIComponent[] components;
    protected int x0, y0, w, h, prevX0, prevY0, prevW, prevH,
            anchorX0, anchorY0,
            anchor = IUIComponent.LEFT | IUIComponent.TOP;
    protected boolean isVisible = true;
    protected boolean isFocused = true;
    protected boolean isActive = true;
    protected int pressedX, pressedY;
    private int bgColor = COLOR_TRANSPARENT;
    private boolean roundBg = false;
    private int padding;
    protected Image bg = null;
    private IUIComponent popupWindow = null;
    protected IContainer parent = null;
    protected IUIComponent draggedEventRecipient = null;
    protected boolean repaintOnlyOnFlushGraphics = false;
    private boolean isCapturingForPopup = false;

    public Container() {
        components = new IUIComponent[0];
    }

    public void init() {
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                components[i].init();
            }
        }
        if (popupWindow != null) {
            popupWindow.init();
        }
    }

    public void postInit() {
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                components[i].postInit();
            }
        }
        if (popupWindow != null) {
            popupWindow.postInit();
        }
    }

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

    public Container setBgImage(Image bg) {
        this.bg = bg;
        return this;
    }

    public Image getCapture() {
        try {
            Image capture = Image.createImage(w, h);
            isCapturingForPopup = true;
            onPaint(capture.getGraphics(), true);
            return capture;
        } catch (Exception ex) {
            ex.printStackTrace();
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

    protected final Container setComponents(IUIComponent[] components) {
        prevW = prevH = 0; // next setBounds call after the components have changed shouldn't be ignored
        if (this.components != null) {
            for (int i = 0; i < this.components.length; i++) {
                if (this.components[i] != null) {
                    this.components[i].setParent(null);
                }
            }
        }

        if (components != null) {
            for (int i = 0; i < components.length; i++) {
                if (components[i] != null) {
                    //System.out.println(getClass().getName() + " set as parent for " + components[i].getClass().getName());
                    components[i].setParent(this);
                    if (hasParent()) {
                        components[i].init();
                    }
                }
            }
        }

        if (popupWindow != null) {
            popupWindow.setParent(this);
            popupWindow.init();
        }

        this.components = components;
        refreshFocusedComponents();
        return this;
    }

    protected final IUIComponent[] getComponents() {
        if (components.length == 0) {
            try {
                throw new IllegalStateException("No components in the container. Did you forgot to call setComponents()? " + getClass().getName());
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
        return components;
    }

    public void showPopup(IUIComponent w) {
        w.setParent(this);
        w.init();
        popupWindow = w;
        refreshFocusedComponents();
        popupWindow.setSize(this.w, h).setPos(x0, y0, TOP | LEFT);
        repaint();
    }

    public void closePopup() {
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

    public void paint(Graphics g) {
        paint(g, false);
    }

    public void paint(Graphics g, boolean forceInactive) {
        paint(g, x0, y0, w, h, forceInactive);
    }

    public void paint(Graphics g, int x0, int y0, int w, int h) {
        paint(g, x0, y0, w, h, false);
    }

    public void paint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (!isVisible) {
            return;
        }

        onPaint(g, x0, y0, w, h, forceInactive);
    }

    private void onPaint(Graphics g, boolean forceInactive) {
        onPaint(g, x0, y0, w, h, forceInactive);
    }

    private void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (w == 0 || h == 0) {
            try {
                throw new Exception("Can't paint: w=" + w + " h=" + h + " " + getClass().getName());
            } catch (Exception ex) {
                ex.printStackTrace();
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

        IUIComponent[] uiComponents = getComponents();

        // TODO move to Graphics and make it similar to how it works in android
        int prevClipX = g.getClipX();
        int prevClipY = g.getClipY();
        int prevClipW = g.getClipWidth();
        int prevClipH = g.getClipHeight();
        int clipX = x0, clipY = y0, clipW = w, clipH = h, clipX2 = clipX + clipW, clipY2 = clipY + clipH;
        clipX = Math.max(clipX, prevClipX);
        clipY = Math.max(clipY, prevClipY);
        clipX2 = Math.min(clipX2, prevClipX + prevClipW);
        clipY2 = Math.min(clipY2, prevClipY + prevClipH);
        clipW = Math.max(0, clipX2 - clipX);
        clipH = Math.max(0, clipY2 - clipY);
        g.setClip(clipX, clipY, clipW, clipH);
        drawBg(g, x0, y0, w, h, forceInactive);
        setBounds(x0, y0, w, h);

        if (popupWindow == null || isCapturingForPopup) {
            for (int i = 0; i < uiComponents.length; i++) {
                try {
                    if (uiComponents[i] != null) {
                        uiComponents[i].paint(g, forceInactive);
                    }
                } catch (Exception ex) { }
            }
            isCapturingForPopup = false;
        } else {
            popupWindow.paint(g, x0, y0, w, h, forceInactive);
        }

        g.setClip(prevClipX, prevClipY, prevClipW, prevClipH);
    }

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

    public IUIComponent refreshSizes() {
        setSize(w++ /* bypass the check (prevW == w) to force refresh */, h);
        return this;
    }

    public boolean canBeFocused() {
        for (int i = components.length - 1; i >= 0; i--) {
            if (components[i] != null) {
                if (components[i].canBeFocused()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isFocused() {
        return isFocused;
    }

    protected void refreshFocusedComponents() {
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                components[i].setFocused(false);
            }
        }

        if (popupWindow != null) {
            popupWindow.setFocused(true);
            return;
        }

        if (isFocused) {
            for (int i = components.length - 1; i >= 0; i--) {
                if (components[i] != null) {
                    if (components[i].canBeFocused()) {
                        components[i].setFocused(true);
                        break;
                    }
                }
            }
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

    public IUIComponent setActive(boolean b) {
        //System.out.println(getClass().getName() + " setActive: " + b);
        isActive = b;
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                components[i].setActive(b);
            }

        }
        return this;
    }

    public boolean toggleIsVisible() {
        setVisible(!isVisible);
        return isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean repaintOnlyOnFlushGraphics() {
        return repaintOnlyOnFlushGraphics;
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

    public boolean pointerClicked(int x, int y) {
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            popupWindow.pointerClicked(x, y);
            if (isTarget) {
                return true;
            }
        }

        IUIComponent[] uiComponents = getComponents();

        try {
            for (int i = uiComponents.length - 1; i >= 0; i--) {
                if (uiComponents[i] == null) {
                    continue;
                }
                if (uiComponents[i].pointerClicked(x, y)) {
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean pointerReleased(int x, int y) {
        if (draggedEventRecipient != null) {
            try {
                draggedEventRecipient.pointerReleased(x, y);
            } catch (Exception ex) {
                Logger.log(ex);
            }
            draggedEventRecipient = null;
            return true;
        }
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            popupWindow.pointerReleased(x, y);
            if (isTarget) {
                return true;
            }
        }

        IUIComponent[] uiComponents = getComponents();

        try {
            for (int i = uiComponents.length - 1; i >= 0; i--) {
                if (uiComponents[i] == null) {
                    continue;
                }
                if (uiComponents[i].pointerReleased(x, y)) {
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean pointerDragged(int x, int y) {
        if (draggedEventRecipient != null) {
            try {
                draggedEventRecipient.pointerDragged(x, y);
            } catch (Exception ex) {
                Logger.log(ex);
            }
            return true;
        }

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
                draggedEventRecipient = popupWindow;
                return true;
            }
        }

        IUIComponent[] uiComponents = getComponents();

        try {
            for (int i = uiComponents.length - 1; i >= 0; i--) {
                if (uiComponents[i] == null) {
                    continue;
                }
                if (uiComponents[i].pointerDragged(x, y)) {
                    draggedEventRecipient = uiComponents[i];
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean pointerPressed(int x, int y) {
        draggedEventRecipient = null;
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        pressedX = x;
        pressedY = y;

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            popupWindow.pointerPressed(x, y);
            if (isTarget) {
                return true;
            }
        }

        IUIComponent[] uiComponents = getComponents();

        try {
            for (int i = uiComponents.length - 1; i >= 0; i--) {
                if (uiComponents[i] == null) {
                    continue;
                }
                if (uiComponents[i].pointerPressed(x, y)) {
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean mouseEvent(int event, int x, int y) {
        if (!checkTouchEvent(x, y)) {
            return false;
        }

        if (popupWindow != null) {
            boolean isTarget = popupWindow.checkTouchEvent(x, y);
            if (isTarget) {
                return popupWindow.mouseEvent(event, x, y); // TODO
            }
        }

        IUIComponent[] uiComponents = getComponents();

        try {
            for (int i = uiComponents.length - 1; i >= 0; i--) {
                if (uiComponents[i] == null) {
                    continue;
                }
                if (uiComponents[i].mouseEvent(event, x, y)) {
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int count) {
        if (!isActive || !isVisible) {
            return false;
        }

        if (popupWindow != null) {
            popupWindow.keyPressed(keyCode, count);
            return true;
        }

        IUIComponent[] uiComponents = getComponents();
        try {
            for (int i = uiComponents.length - 1; i >= 0; i--) {
                if (uiComponents[i] == null) {
                    continue;
                }
                if (uiComponents[i].keyPressed(keyCode, count)) {
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean keyReleased(int keyCode, int count) {
        if (!isActive || !isVisible) {
            return false;
        }

        if (popupWindow != null) {
            popupWindow.keyReleased(keyCode, count);
            return true;
        }

        IUIComponent[] uiComponents = getComponents();
        try {
            for (int i = uiComponents.length - 1; i >= 0; i--) {
                if (uiComponents[i] == null) {
                    continue;
                }
                if (uiComponents[i].keyReleased(keyCode, count)) {
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean keyRepeated(int keyCode, int pressedCount) {
        if (!isActive || !isVisible) {
            return false;
        }

        if (popupWindow != null) {
            popupWindow.keyRepeated(keyCode, pressedCount);
            return true;
        }

        IUIComponent[] uiComponents = getComponents();
        try {
            for (int i = uiComponents.length - 1; i >= 0; i--) {
                if (uiComponents[i] == null) {
                    continue;
                }
                if (uiComponents[i].keyRepeated(keyCode, pressedCount)) {
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void onShow() { }
    public void onHide() { }

    public IUIComponent setPos(int x0, int y0) {
        this.x0 = x0;
        this.y0 = y0;
        return this;
    }

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

        setPos(x0, y0);
        setBounds(x0, y0, w, h);

        return this;
    }

    public IUIComponent setSize(int w, int h) {
        if (w == 0 || h == 0) {
            try {
                throw new Exception("Setting zero size (" + w + ", " + h + ") in" + getClass().getName());
            } catch (Exception ex) {
                Logger.log(ex);
            }
            return this;
        }

        if (this.w == w && this.h == h) {
            Logger.log(w + " " + h + " has not changed (" + getClass().getName() + ")");
            return this;
        }

        setBounds(x0, y0, w, h);
        setPos(anchorX0, anchorY0, anchor);

        return this;
    }

    private final void setBounds(int x0, int y0, int w, int h) {
        if (w == 0 || h == 0) {
            try {
                throw new Exception("Setting zero size (" + w + ", " + h + ") in" + getClass().getName());
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
                ex.printStackTrace();
            }
            return null;
        }
    }

    public void repaint() {
        if (!isVisible) {
            return;
        }

        if (parent != null) {
            parent.repaint();
        } else {
            try {
                throw new NullPointerException("Can't call parent's repaint: parent component is not set! " + getClass().getName());
            } catch (NullPointerException ex) {
                ex.printStackTrace();
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
                ex.printStackTrace();
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
                ex.printStackTrace();
            }
        }
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

    public boolean isOnScreen() {
        return hasParent() && parent.isOnScreen();
    }

    protected abstract void onSetBounds(int x0, int y0, int w, int h);

}
