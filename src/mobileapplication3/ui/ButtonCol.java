/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class ButtonCol extends AbstractButtonSet {
    
    private AnimationThread animationThread = null;
    private int btnH = H_AUTO;
    
    private boolean isScrollable = true;
    private boolean trimHeight = true;
    private int hBeforeTrim, btnHBeforeAuto = btnH, prevTotalBtnsH;
    private int scrollOffset = 0;
    private int pointerPressedY, scrollOffsetWhenPressed;
    protected int lastDraggedY, lastDraggedDY, draggedAvgDY;
    protected long lastDraggedT;
    protected int lastDraggedDT, draggedAvgDT;
    private boolean startFromBottom = false;
    private boolean kbSmoothScrolling = true, kineticTouchScrolling = true;
    
    public ButtonCol() {
    	setIsSelectionEnabled(true);
    }

    public ButtonCol(Button[] buttons) {
    	this();
        this.buttons = buttons;
    }
    
    public void init() {
    	try {
    		kbSmoothScrolling = getUISettings().getKbSmoothScrollingEnabled();
    		kineticTouchScrolling = getUISettings().getKineticTouchScrollingEnabled();
    	} catch (Exception ex) { }
    	super.init();
    }

    public void recalcSize() {
        setSizes(w, hBeforeTrim, btnHBeforeAuto, trimHeight);
    }

    public IUIComponent setSize(int w, int h) {
        return setSizes(w, h, btnHBeforeAuto);
    }
    
    public IUIComponent setSizes(int w, int h, int btnH) {
        return setSizes(w, h, btnH, trimHeight);
    }

    public IUIComponent setSizes(int w, int h, int btnH, boolean trimHeight) {
        if (w == 0 || h == 0 || btnH == 0) {
            try {
                throw new Exception("Setting zero as a dimension " + getClass().getName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return this;
        }
        
        int prevH = this.h;
        super.setSize(w, h);
        this.hBeforeTrim = this.h;
        this.btnHBeforeAuto = btnH;
        this.btnH = btnH;
        this.trimHeight = trimHeight;
        
        if (buttons == null) {
            return this;
        }
        
        if (this.w == W_AUTO) {
            this.w = getMinPossibleWidth();
        }
        
        if (this.btnH == H_AUTO) {
        	this.btnH = Font.getDefaultFont().getHeight() * 5 / 2 + buttonsBgPadding*2;
            if (this.h != H_AUTO && !this.trimHeight) {
                if (buttons.length > 0) {
                    this.btnH = Math.max(this.btnH, this.h / buttons.length);
                }
            }
        } else {
            this.h = Math.min(this.h, this.btnH * buttons.length);
        }
        
        if (this.h == H_AUTO) {
            this.h = buttons.length * this.btnH;
        }
        
        if (this.trimHeight) {
            this.h = Math.min(this.h, this.btnH * buttons.length);
        }
        
        if (startFromBottom) {
            int dtbh = getTotalBtnsH() - prevTotalBtnsH;
            int dh = this.h - prevH;
            prevTotalBtnsH = getTotalBtnsH();
            
            scrollOffset += dtbh - dh;
            
            setSelected(buttons.length - 1);
        }
        
        scrollOffsetWhenPressed = scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalBtnsH() - this.h));
        
        recalcPos();
        return this;
    }
    
    public int getMinPossibleWidth() { /////// need fix
        int res = 0;
        for (int i = 0; i < buttons.length; i++) {
            String[] btnTextLines = Utils.split(buttons[i].getTitle(), "\n");
            for (int j = 0; j < btnTextLines.length; j++) {
                res = Math.max(res, Font.getDefaultFont().stringWidth(btnTextLines[j] + "  ") + buttons[i].getBgPagging()*4);
            }
        }
        return res;
    }

    public int getBtnH() {
        return btnH;
    }
    
    public int getTotalBtnsH() {
        if (buttons == null) {
            return 0;
        }
        
        return buttons.length * getBtnH();
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
            animationThread.animate(0, this.scrollOffset, 0, scrollOffset, 200, 0, 0, 0, btnH*buttons.length - h);
        } else {
            this.scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalBtnsH() - h));
        }
        return true;
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
        x -= x0;
        y -= y0 - scrollOffset;
        setSelected(y / btnH);
        boolean wasSelected = (selected == prevSelected && isSelectionEnabled);
        
        return buttons[selected].invokePressed(wasSelected, isFocused);
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
        int bottomLimitY = getTotalBtnsH() - h;

        if (kineticTouchScrolling && targetY != startY) {
            initAnimationThread();
            int t = Math.min(2000, Math.abs(2 * dY * draggedAvgDT / draggedAvgDY));
            animationThread.animate(0, startY, 0, targetY, t, 0, 0, topLimitY, bottomLimitY, draggedAvgDT);
        }
        
        return true;
    }
    
    public boolean handlePointerPressed(int x, int y) {
    	stopAnimation();

        if (!isVisible) {
            return false;
        }
        
        if (buttons == null || buttons.length == 0) {
            return false;
        }

        if (btnH*buttons.length <= h) {
            //return false;
        }
        
        if (!checkTouchEvent(x, y)) {
            pointerPressedY = -1;
            return false;
        }
        
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
        
        if (buttons == null || buttons.length == 0) {
            return false;
        }
        
        if (!isScrollable) {
            return false;
        }
        
        if (btnH*buttons.length <= h) {
            //return false;
        }
        
        scrollOffset = scrollOffsetWhenPressed - (y - pointerPressedY);
        
        scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalBtnsH() - h));

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

    public boolean onKeyPressed(int keyCode, int count) {
        if (!isVisible) {
            return false;
        }
        
        if (!isSelectionEnabled) {
            return false;
        }
        
        if (buttons == null || buttons.length == 0) {
            return false;
        }
        
        boolean ret = false;
        
        int action = RootContainer.getAction(keyCode);
        do {
	        switch (action) {
	            case Keys.UP:
	                if (selected > 0) {
	                    setSelected(selected-1);
	                } else {
	                	setSelected(buttons.length - 1);
	                }
	                ret = true;
	                break;
	            case Keys.DOWN:
	                if (selected < buttons.length - 1) {
	                	setSelected(selected+1);
	                } else {
	                	setSelected(0);
	                }
	                ret = true;
	                break;
	            case Keys.FIRE:
	                if (isSelectionEnabled) {
	                    isSelectionVisible = true;
	                }
	                return buttons[selected].invokePressed(true, isFocused);
	        }
        } while (buttons[selected] instanceof ButtonStub && action != Keys.FIRE);
        
        if (isSelectionEnabled) {
            isSelectionVisible = true;
        }

        return ret;
    }

    public AbstractButtonSet setSelected(int selected) {
        int selectedY = btnH * selected;
        int startY = scrollOffset;
        int targetY = scrollOffset;
        int topLimitY = 0;
        int bottomLimitY = btnH * buttons.length - h;
        if (btnH * 2 < h) {
            if (selectedY < scrollOffset) {
                targetY = Math.max(topLimitY, selectedY - btnH * 3 / 4);
            }

            if (selectedY + btnH > scrollOffset + h) {
                targetY = Math.min(bottomLimitY, selectedY - h + btnH + btnH * 3 / 4);
            }
        } else {
            // if buttons are bigger than the screen, just center the targeted one
            targetY = selectedY - h / 2 + btnH / 2;
        }

        if (kbSmoothScrolling && targetY != startY) {
            initAnimationThread();
            animationThread.animate(0, startY, 0, targetY, 200, 0, 0, topLimitY, bottomLimitY);
        } else {
            scrollOffset = targetY;
        }
        return super.setSelected(selected);
    }

    public ButtonCol enableScrolling(boolean isScrollable, boolean startFromBottom) {
        this.startFromBottom = startFromBottom;
        
        if (isScrollable) {
            setIsSelectionEnabled(true);
        }
        
        this.isScrollable = isScrollable;
        return this;
    }

    public ButtonCol enableAnimations(boolean b) {
        kbSmoothScrolling = b;
        return this;
    }

    public ButtonCol trimHeight(boolean b) {
        trimHeight = b;
        return this;
    }
    
    public void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (buttons == null || buttons.length == 0) {
            return;
        }

        for (int i = 0; i < buttons.length; i++) {
        	int prevFontFace = g.getFontFace();
			int prevFontStyle = g.getFontStyle();
			int prevFontSize = g.getFontSize();

            int btnY = y0 - scrollOffset + i* this.btnH;

            if (btnY + this.btnH < y0) {
                continue;
            }
            
            if (btnY > y0 + h) {
                break;
            }
            
            boolean drawAsSelected = (i == selected && isSelectionVisible && isFocused);
            buttons[i].paint(g, x0, btnY, w, this.btnH, x0, btnY, w, this.btnH, drawAsSelected, isFocused, forceInactive, showKbHints);
            g.setFont(prevFontFace, prevFontStyle, prevFontSize);
        }
        
        if (isSelectionEnabled && h < getTotalBtnsH()) {
            g.setColor(0xffffff);
            int scrollBarMarkY0 = h * scrollOffset / getTotalBtnsH();
            int scrollBarMarkY1 = h * (scrollOffset + h) / getTotalBtnsH();
            g.drawLine(x0 + w - 1, y0 + scrollBarMarkY0, x0 + w - 1, y0 + scrollBarMarkY1);
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

    private void stopAnimation() {
        if (animationThread != null) {
            animationThread.stop();
        }
    }
}
