// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;

/**
 *
 * @author vipaol
 */
public class Grid extends UIComponent implements IContainer {


    //public static final int W_AUTO = -1;
    public static final int H_AUTO = -1;

    public IUIComponent[] elements = null;
    private int cols = 1;
    protected int bgColor = COLOR_TRANSPARENT;
    protected int elementsBgColor = NOT_SET;
    protected int elementsPadding = 0;

    protected int selected = 0;
    protected int prevSelected = 0;
    protected boolean isSelectionEnabled = true;
    protected boolean isSelectionVisible = false;
    protected boolean selectedOutOfRange = false;

    private AnimationThread animationThread = null;
    private int elemW;
    private int elemH = H_AUTO;

    private boolean isScrollable = true;
    private boolean trimHeight = true, autoElemH = false;
    private int hBeforeTrim, prevTotalelemsH;
    private int scrollOffset = 0;
    protected int pointerPressedX, pointerPressedY, scrollOffsetWhenPressed;
    protected int lastDraggedY, lastDraggedDY, draggedAvgDY;
    protected long lastDraggedT;
    protected int lastDraggedDT, draggedAvgDT;
    private boolean startFromBottom;
    private boolean kbSmoothScrolling = true, kineticTouchScrolling = true;
    private boolean isInited = false;
    protected boolean ignoreKeyRepeated = true;

    public Grid() { }

    public Grid(IUIComponent[] elements) {
        this.elements = elements;
    }

    public void init() {
        try {
            ignoreKeyRepeated = !getUISettings().getKeyRepeatedInListsEnabled();
            isSelectionVisible = getUISettings().showKbHints();
            kbSmoothScrolling = getUISettings().getKbSmoothScrollingEnabled();
            kineticTouchScrolling = getUISettings().getKineticTouchScrollingEnabled();
        } catch (Exception ex) { }

        isInited = true;
        setElements(elements);
    }

    public void recalcSize() {
        setSizes(w, hBeforeTrim, elemH, trimHeight);
    }

    public Grid setCols(int cols) {
        this.cols = cols;
        return this;
    }

    public IUIComponent setSize(int w, int h) {
        return setSizes(w, h, elemH);
    }

    public IUIComponent setSizes(int w, int h, int elemH) {
        return setSizes(w, h, elemH, trimHeight);
    }

    public IUIComponent setSizes(int w, int h, int elemH, boolean trimHeight) {
        if (w == 0 || h == 0 || elemH == 0) {
            try {
                throw new Exception("Setting zero as a dimension " + getClass().getName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return this;
        }

        int prevH = this.h;
        this.w = w;
        this.h = h;
        this.elemH = elemH;
        this.hBeforeTrim = this.h;
        this.trimHeight = trimHeight;
        this.autoElemH = this.autoElemH || this.elemH == H_AUTO;

        if (elements == null) {
            return this;
        }

        elemW = w / cols;

        if (autoElemH) {
            this.elemH = elemW;
        }

        if (this.h == H_AUTO) {
            this.h = getTotalElemsH();
        }

        if (this.trimHeight) {
            this.h = Math.min(this.h, getTotalElemsH());
        }

        if (startFromBottom) {
            int dteh = getTotalElemsH() - prevTotalelemsH;
            int dh = this.h - prevH;
            prevTotalelemsH = getTotalElemsH();

            scrollOffset += dteh - dh;

            setSelected(elements.length - 1);
        }

        scrollOffsetWhenPressed = scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalElemsH() - this.h));

        recalcPos();

        return super.setSize(this.w, this.h);
    }

    protected void onSetBounds(int x0, int y0, int w, int h) {
        for (int i = 0; i < elements.length; i++) {
            elements[i].setSize(w, elemH).setPos(x0, y0 + i * elemH, LEFT | TOP);
        }
    }

//    public int getMinPossibleWidth() { /////// need fix
//        int res = 0;
//        for (int i = 0; i < elements.length; i++) {
//            res = elements[i].getMinPossibleWidth();
//        }
//        return res;
//    }

    public int getElemH() {
        return elemH;
    }

    public int getRowsCount() {
        if (elements == null) {
            return 0;
        }
        int c = elements.length / cols;
        if (elements.length % cols != 0) {
            c++;
        }
        return c;
    }

    public int getTotalElemsH() {
        return getRowsCount() * getElemH();
    }

    public boolean handleMouseEvent(int event, int x, int y) {
        int scrollOffset = this.scrollOffset;
        if (event == MOUSE_WHEEL_SCROLLED_DOWN) {
            scrollOffset += h / 5;
        } else if (event == MOUSE_WHEEL_SCROLLED_UP) {
            scrollOffset -= h / 5;
        } else {
            return false;
        }
        if (kbSmoothScrolling && scrollOffset != this.scrollOffset) {
            initAnimationThread();
            animationThread.animate(0, this.scrollOffset, 0, scrollOffset, 200, 0, 0, 0, getTotalElemsH() - h);
        } else {
            this.scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalElemsH() - h));
        }
        return true;
    }

    public boolean handlePointerClicked(int x, int y) {
        if (!isVisible) {
            return false;
        }

        if (elements == null || elements.length == 0) {
            return false;
        }

        if (!checkTouchEvent(x, y)) {
            return false;
        }

        prevSelected = selected;
        int selected = (y - y0 + scrollOffset) / elemH * cols + x / elemW;
        selectedOutOfRange = selected >= elements.length;
        if (!selectedOutOfRange) {
            setSelected(selected);
            elements[selected].pointerPressed(x, y);
        }

        return !selectedOutOfRange && elements[selected].pointerClicked(x, y);
    }

    protected boolean handlePointerReleased(int x, int y) {
        int startY = scrollOffset;
        int targetY = scrollOffset;

        int dY;
        if (draggedAvgDT > 0) {
            dY = draggedAvgDY * draggedAvgDY * 350 / draggedAvgDT / draggedAvgDT;
        } else {
            dY = 0;
        }
        targetY -= dY * Mathh.sign(draggedAvgDY);
        int topLimitY = 0;
        int bottomLimitY = getTotalElemsH() - h;

        if (kineticTouchScrolling && targetY != startY) {
            initAnimationThread();
            int t = Math.min(2000, Math.abs(2 * dY * draggedAvgDT / draggedAvgDY));
            animationThread.animate(0, startY, 0, targetY, t, 0, 0, topLimitY, bottomLimitY, draggedAvgDT);
        } else {
            scrollOffset = Mathh.constrain(topLimitY, targetY, bottomLimitY);
        }

        return true;
    }

    public boolean handlePointerPressed(int x, int y) {
        stopAnimation();

        if (!isVisible) {
            return false;
        }

        if (elements == null || elements.length == 0) {
            return false;
        }

        if (!isScrollable) {
            return false;
        }

        if (!checkTouchEvent(x, y)) {
            pointerPressedX = pointerPressedY = -1;
            return false;
        }

        pointerPressedX = x;
        pointerPressedY = y;
        scrollOffsetWhenPressed = scrollOffset;

        lastDraggedY = y;
        lastDraggedT = System.currentTimeMillis();
        draggedAvgDY = 0;
        draggedAvgDT = 0;

        return true;
    }

    public boolean handlePointerDragged(int x, int y) {
        if (!isVisible) {
            return false;
        }

        if (elements == null || elements.length == 0) {
            return false;
        }

        if (!isScrollable) {
            return false;
        }

        if (!selectedOutOfRange && elements[selected].pointerDragged(x, y)) {
            return true;
        }

        scrollOffset = scrollOffsetWhenPressed - (y - pointerPressedY);

        scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalElemsH() - h));

        lastDraggedDY = y - lastDraggedY;
        lastDraggedY = y;

        lastDraggedDT = (int) (System.currentTimeMillis() - lastDraggedT);
        lastDraggedT = System.currentTimeMillis();

        if (draggedAvgDY == 0) {
            draggedAvgDY = lastDraggedDY;
        } else {
            draggedAvgDY = (2 * draggedAvgDY + 2 * lastDraggedDY + 1) / 4;
        }

        if (draggedAvgDT == 0) {
            draggedAvgDT = lastDraggedDT;
        } else {
            draggedAvgDT = (2 * draggedAvgDT + 2 * lastDraggedDT + 1) / 4;
        }

        return true;
    }

    public boolean handleKeyPressed(int keyCode, int count) {
        if (!isVisible) {
            return false;
        }

        if (!isSelectionEnabled) {
            return false;
        }

        if (elements == null || elements.length == 0) {
            return false;
        }

        if (handleKeyPressedScrollOnly(keyCode, count, false)) {
            return true;
        } else {
            return elements[selected].keyPressed(keyCode, count);
        }
    }

    private boolean handleKeyPressedScrollOnly(int keyCode, int count, boolean isKeyRepeated) {
        if (ignoreKeyRepeated && isKeyRepeated) {
            return false;
        }
        switch (keyCode) {
        default:
            switch (RootContainer.getAction(keyCode)) {
                case Keys.LEFT:
                    if (selected > 0) {
                        setSelected(selected-1);
                    } else {
                        setSelected(elements.length - 1);
                    }
                    break;
                case Keys.RIGHT:
                    if (selected < elements.length - 1) {
                        setSelected(selected+1);
                    } else {
                        setSelected(0);
                    }
                    break;
                case Keys.UP:
                    if (selected > cols - 1) {
                        setSelected(selected-cols);
                    } else {
                        int i = elements.length / cols * cols + selected % cols;
                        if (i < elements.length) {
                            setSelected(i);
                        } else {
                            setSelected(i - cols);
                        }
                    }
                    break;
                case Keys.DOWN:
                    if (selected < elements.length - cols) {
                        setSelected(selected+cols);
                    } else {
                        setSelected(selected % cols);
                    }
                    break;
                default:
                    return false;
            }
        }

        int selectedY = selected / cols * elemH;
        int startY = scrollOffset;
        int targetY = scrollOffset;
        if (elemH * 2 < h) {
            if (selectedY < scrollOffset) {
                targetY = selectedY - elemH * 3 / 4;
            }

            if (selectedY + elemH > scrollOffset + h) {
                targetY = selectedY - h + elemH + elemH * 3 / 4;
            }
        } else {
            // if elements are bigger than the screen, just center the targeted one
            targetY = selectedY - h / 2 + elemH / 2;
        }

        int topLimitY = 0;
        int bottomLimitY = getTotalElemsH() - h;

        if (kbSmoothScrolling && targetY != startY) {
            initAnimationThread();
            animationThread.animate(0, startY, 0, targetY, 200, 0, 0, topLimitY, bottomLimitY);
        }

        if (isSelectionEnabled) {
            isSelectionVisible = true;
        }

        return true;
    }

    public Grid enableScrolling(boolean isScrollable, boolean startFromBottom) {
        this.startFromBottom = startFromBottom;

        if (isScrollable) {
            setIsSelectionEnabled(true);
        }

        this.isScrollable = isScrollable;
        return this;
    }

    public Grid trimHeight(boolean b) {
        trimHeight = b;
        return this;
    }

    public void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (elements == null || elements.length == 0) {
            return;
        }

        for (int i = 0; i < elements.length; i++) {
            Font prevFont = g.getFont();
            int elemW = this.elemW - elementsPadding * 2;
            int elemH = this.elemH - elementsPadding * 2;
            int cellX = i % cols;
            int cellY = i / cols;
            int elemX = x0 + cellX * this.elemW + elementsPadding;
            int elemY = y0 - scrollOffset + cellY*this.elemH + elementsPadding;
            int bottomY = y0 + h;

            if (elemY + elemH < y0) {
                continue;
            }

            if (elemY > y0 + h) {
                break;
            }

            boolean drawAsSelected = (i == selected && isSelectionVisible && isFocused);
            drawBgUnderElement(g, elemX, elemY, elemW, elemH, !forceInactive, drawAsSelected);
            elements[i].paint(g, elemX, elemY, elemW, elemH, forceInactive);
            g.setFont(prevFont);

            if (drawAsSelected) {
                g.setColor(0xffffff);
                int markY0 = elemH / 2 - Font.getDefaultFontHeight()/2;
                int markY1 = elemH - markY0;
                int markCenterY = (markY0 + markY1) / 2;
                int markw = (markY1 - markY0) / 2;
                g.fillTriangle(elemX + 1, elemY + markY0, elemX + 1, elemY + markY1, elemX + markw, elemY + markCenterY);
                g.fillTriangle(elemX + elemW - 1, elemY + markY0, elemX + elemW - 1, elemY + markY1, elemX + elemW - markw, elemY + markCenterY);
            }
        }

        if (isSelectionEnabled && !forceInactive && h < getTotalElemsH()) {
            g.setColor(0xffffff);
            int selectionMarkY0 = h * scrollOffset / getTotalElemsH();
            int selectionMarkY1 = h * (scrollOffset + h) / getTotalElemsH();
            g.drawLine(x0 + w - 1, y0 + selectionMarkY0, x0 + w - 1, y0 + selectionMarkY1);
        }
    }

    protected void drawBgUnderElement(Graphics g, int x0, int y0, int w, int h, boolean isActive, boolean isSelected) {
        if (isActive) {
            if (isSelected) {
                g.setColor(COLOR_ACCENT);
            } else {
                g.setColor(COLOR_ACCENT_MUTED);
            }
        } else {
            g.setColor(BG_COLOR_INACTIVE);
        }

        int d = Math.min(w/5, h/5);
        g.fillRoundRect(x0, y0, w, h, d, d);
    }

    private void initAnimationThread() {
        if (animationThread == null) {
            animationThread = new AnimationThread(new AnimationThread.AnimationWorker() {
                public void onStep(int newX, int newY) {
                    scrollOffset = newY;
                    repaint();
                }
            });
        }
    }

    private void stopAnimation() {
        if (animationThread != null) {
            animationThread.stop();
        }
    }

    public Grid setElements(IUIComponent[] elements) {
        this.elements = elements;
        if (!isInited || elements == null) {
            return this;
        }

        for (int i = 0; i < elements.length; i++) {
            elements[i].setParent(this);
            elements[i].init();
            elements[i].setBgColor(COLOR_TRANSPARENT);
            if (elements[i] instanceof AbstractButtonSet) {
                ((AbstractButtonSet) elements[i]).setIsSelectionEnabled(true);
            }
        }

//        setElementsBgColor(elementsBgColor);
//        setSelectedColor(elementsSelectedColor);
        setElementsPadding(elementsPadding);
        setIsSelectionEnabled(isSelectionEnabled);

        if (isSizeSet()) {
            recalcSize();
        }

        return this;
    }

    public Grid setElementsBgColor(int color) {
        if (color == NOT_SET) {
            return this;
        }

        this.elementsBgColor = color;
        if (elements == null) {
            return this;
        }

        for (int i = 0; i < elements.length; i++) {
            elements[i].setBgColor(color);
        }
        return this;
    }

    public Grid setElementsPadding(int padding) {
        elementsPadding = padding;
        if (elements == null) {
            return this;
        }

        for (int i = 0; i < elements.length; i++) {
            //elements[i].setPadding(padding);
        }
        return this;
    }

    public Grid setIsSelectionEnabled(boolean selectionEnabled) {
        this.isSelectionEnabled = selectionEnabled;
        return this;
    }

    public Grid setIsSelectionVisible(boolean isSelectionVisible) {
        this.isSelectionVisible = isSelectionVisible;
        return this;
    }

    public Grid setSelected(int selected) {
        // TODO add check
        this.selected = selected;
        return this;
    }

    public int getSelected() {
        return selected;
    }

    public int getElementCount() {
        if (elements != null) {
            return elements.length;
        } else {
            return 0;
        }
    }

    public boolean canBeFocused() {
        if (elements == null) {
            return false;
        }

        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null) {
                if (elements[i].canBeFocused()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean handleKeyRepeated(int keyCode, int pressedCount) {
        if (handleKeyPressedScrollOnly(keyCode, 1, true)) {
            return true;
        } else {
            return elements[selected].keyRepeated(keyCode, pressedCount) || isFocused;
        }
    }
}
