// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.Utils;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;

/**
 *
 * @author vipaol
 */
public class ButtonRow extends AbstractButtonSet {
    private AnimationThread animationThread = null;

    private int leftSoftBindIndex = NOT_SET;
    private int rightSoftBindIndex = NOT_SET;

    private int btnW = NOT_SET;
    private int scrollOffset = 0;
    private int pointerPressedX = -1, pointerPressedY = -1;
    private int scrollOffsetWhenPressed;
    private boolean isScrollable = false;
    private boolean isDraggedEventRecipient = false;
    private boolean kbSmoothScrolling = true;

    public ButtonRow() { }

    public ButtonRow(Button[] buttons) {
        this.buttons = buttons;
    }

    public void init() {
        try {
            kbSmoothScrolling = getUISettings().getKbSmoothScrollingEnabled();
        } catch (Exception ignored) { }
        super.init();
    }

    public ButtonRow bindToSoftButtons() {
        return bindToSoftButtons(0, buttons.length - 1);
    }

    public ButtonRow bindToSoftButtons(int leftSoftBindIndex, int rightSoftBindIndex) {
        this.leftSoftBindIndex = leftSoftBindIndex;
        this.rightSoftBindIndex = rightSoftBindIndex;
        return this;
    }

    public int getMinPossibleWidth() { // need fix
        int res = 0;
        for (int i = 0; i < buttons.length; i++) {
            String[] btnTextLines = Utils.split(buttons[i].getTitle(), "\n");
            int maxLineW = 0;
            for (int j = 0; j < btnTextLines.length; j++) {
                maxLineW = Math.max(maxLineW, Font.getDefaultFont().stringWidth(btnTextLines[j] + "    ") + buttons[i].getBgPadding()*4);
            }
            res += maxLineW;
        }
        return res;
    }

    public void onSetBounds(int x0, int y0, int w, int h) {
        if (w == W_AUTO) {
            this.w = getMinPossibleWidth();
        }
        if (h == H_AUTO) {
            this.h = Font.getDefaultFont().getHeight() * 5 / 2 + buttonsBgPadding;
        }

        if (buttons != null && buttons.length > 0) {
            int btnW;
            if (isScrollable) {
                int minPossibleWidth = getMinPossibleWidth();
                btnW = minPossibleWidth / buttons.length;

                int minBtnW = Font.getDefaultFont().stringWidth(" 0000 ");
                if (btnW < minBtnW) {
                    btnW = minBtnW;
                }

                if (btnW * buttons.length < w) {
                    btnW = w / buttons.length;
                }
            } else {
                btnW = w / buttons.length;
            }
            setBtnW(btnW);
        }
        scrollOffset = Mathh.constrain(0, scrollOffset, getMaxScrollOffset());
    }

    public ButtonRow enableScrolling(boolean b) {
        this.isScrollable = b;
        return this;
    }

    private int getMaxScrollOffset() {
        return Math.max(0, getTotalBtnsW() - (w - padding * 2));
    }

    public void setBtnW(int btnW) {
        this.btnW = btnW;
    }

    public int getBtnW() {
        return Math.max(1, btnW);
    }

    private int getTotalBtnsW() {
        if (buttons == null) {
            return 0;
        }

        return buttons.length * getBtnW();
    }

    public boolean handlePointerClicked(int x, int y) {
        if (!isVisible) {
            return false;
        }

        if (buttons == null || buttons.length == 0) {
            return false;
        }

        if (!checkTouchEvent(x, y)) {
            return false;
        }

        prevSelected = selected;
        x -= x0 - scrollOffset;
        y -= y0;
        setSelected(x / getBtnW());
        boolean wasSelected = (selected == prevSelected && isSelectionEnabled);

        return buttons[selected].invokePressed(wasSelected, isFocused);
    }

    public boolean handlePointerPressed(int x, int y) {
        if (!isVisible) {
            return false;
        }

        if (buttons == null || buttons.length == 0) {
            return false;
        }

        if (!checkTouchEvent(x, y)) {
            pointerPressedX = -1;
            pointerPressedY = -1;
            return false;
        }

        pointerPressedX = x;
        pointerPressedY = y;
        isDraggedEventRecipient = false;
        scrollOffsetWhenPressed = scrollOffset;
        return true;
    }

    public boolean handlePointerReleased(int x, int y) {
        pointerPressedX = -1;
        pointerPressedY = -1;
        boolean wasDragged = isDraggedEventRecipient;
        isDraggedEventRecipient = false;
        return wasDragged;
    }

    public boolean handlePointerDragged(int x, int y) {
        if (pointerPressedX < 0 || pointerPressedY < 0 || !isScrollable) {
            return false;
        }

        int dx = x - pointerPressedX;
        int dy = y - pointerPressedY;

        if (!isDraggedEventRecipient && Math.abs(dy) > Math.abs(dx * 5)) {
            pointerPressedX = -1;
            return false; // give up focus to parent
        }

        isDraggedEventRecipient = true;
        scrollOffset = Mathh.constrain(0, scrollOffsetWhenPressed - dx, getMaxScrollOffset());
        return true;
    }

    protected boolean handleBindsOnKeyPressed(int keyCode) {
        switch (keyCode) {
        case KEYCODE_LEFT_SOFT:
            if (leftSoftBindIndex != NOT_SET) {
                return buttons[leftSoftBindIndex].invokePressed(selected == leftSoftBindIndex, isFocused);
            }
        case KEYCODE_RIGHT_SOFT:
            if (rightSoftBindIndex != NOT_SET) {
                return buttons[rightSoftBindIndex].invokePressed(selected == rightSoftBindIndex, isFocused);
            }
        }
        return super.handleBindsOnKeyPressed(keyCode);
    }

    public boolean onKeyPressed(int keyCode, int count) {
        if (!isVisible) {
            return false;
        }

        if (buttons == null || buttons.length == 0) {
            return false;
        }

        switch (keyCode) {
            default:
                if (!isSelectionEnabled) {
                    return false;
                }
                switch (RootContainer.getAction(keyCode)) {
                    case Keys.LEFT:
                        if (selected > 0) {
                            setSelected(selected-1);
                        } else {
                            setSelected(buttons.length - 1);
                        }
                        break;
                    case Keys.RIGHT:
                        if (selected < buttons.length - 1) {
                            setSelected(selected+1);
                        } else {
                            setSelected(0);
                        }
                        break;
                    case Keys.FIRE:
                        return buttons[selected].invokePressed(true, isFocused);
                    default:
                        return isFocused;
                }
        }

        // scrolling is not implemented for rows (maybe yet)
//        int selectedH = btnH * selected;
//        if (selectedH - btnH < scrollOffset) {
//            initAnimationThread();
//            animationThread.animate(0, scrollOffset, 0, Math.max(0, selectedH - btnH * 3 / 4), 200);
//        }
//
//        if (selectedH + btnH > scrollOffset + h) {
//            initAnimationThread();
//            animationThread.animate(0, scrollOffset, 0, Math.min(btnH*buttons.length - h, selectedH - h + btnH + btnH * 3 / 4), 200);
//        }

        if (isSelectionEnabled) {
            isSelectionVisible = true;
        }

        return true;
    }

    public AbstractButtonSet setSelected(int selected) {
        int btnW = getBtnW();
        int selectedX = btnW * selected;
        int startX = scrollOffset;
        int targetX = scrollOffset;
        int leftLimitX = 0;
        int rightLimitX = getMaxScrollOffset();
        if (btnW * 2 < w) {
            if (selectedX < scrollOffset) {
                targetX = Math.max(leftLimitX, selectedX - btnW * 3 / 4);
            }

            if (selectedX + btnW > scrollOffset + w) {
                targetX = Math.min(rightLimitX, selectedX - w + btnW + btnW * 3 / 4);
            }
        } else {
            // if buttons are bigger than the screen, just center the targeted one
            targetX = selectedX - h / 2 + btnW / 2;
        }

        if (kbSmoothScrolling && targetX != startX) {
            initAnimationThread();
            animationThread.animate(0, startX, 0, targetX, 200, 0, 0, leftLimitX, rightLimitX);
        } else {
            scrollOffset = targetX;
        }
        return super.setSelected(selected);
    }

    public void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (buttons == null || buttons.length == 0) {
            return;
        }

        int btnW = getBtnW();
        for (int i = 0; i < buttons.length; i++) {
            int btnX = x0 - scrollOffset + i * btnW;

            if (btnX + btnW < x0) {
                continue;
            }
            if (btnX > x0 + w) {
                break;
            }

            boolean drawAsSelected = i == selected && isSelectionEnabled && isSelectionVisible;
            buttons[i].paint(g, btnX, y0, btnW, h, btnX, y0, btnW, h, drawAsSelected, forceInactive, showKbHints);
        }
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
}