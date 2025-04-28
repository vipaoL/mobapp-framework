package mobileapplication3.ui;

import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;

/**
 *
 * @author vipaol
 */
public class List extends UIComponent implements IContainer {
    
    
    //public static final int W_AUTO = -1;
    public static final int H_AUTO = -1;
    
    public IUIComponent[] elements = null;
    protected int bgColor = COLOR_TRANSPARENT;
    protected int elementsBgColor = NOT_SET;
    protected int elementsPadding = 0;
    
    protected int selected = 0;
    protected int prevSelected = 0;
    protected boolean isSelectionEnabled = true;
    protected boolean isSelectionVisible = false;
    
    private AnimationThread animationThread = null;
    private int elemH = H_AUTO;
    
    private boolean isScrollable = true;
    private boolean trimHeight = true;
    private int hUntilTrim, prevTotalelemsH;
    private int scrollOffset = 0;
    protected int pointerPressedX, pointerPressedY, scrollOffsetWhenPressed;
    private boolean startFromBottom = false;
    private boolean enableAnimations = true;
    private boolean isInited = false;
    protected boolean ignoreKeyRepeated = true;
    
    public List() { }

    public List(IUIComponent[] elements) {
    	this.elements = elements;
    }
    
    public void init() {
    	try {
    		ignoreKeyRepeated = !getUISettings().getKeyRepeatedInListsEnabled();
    		isSelectionVisible = isSelectionVisible || getUISettings().showKbHints();
    	} catch (Exception ex) { }
    	
    	isInited = true;
    	setElements(elements);
    }

    public void recalcSize() {
        setSizes(w, hUntilTrim, elemH, trimHeight);
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
        this.hUntilTrim = this.h;
        this.trimHeight = trimHeight;
        
        if (elements == null) {
            return this;
        }
        
//        if (this.w == W_AUTO) {
//            this.w = getMinPossibleWidth();
//        }
        
        if (this.elemH == H_AUTO) {
        	this.elemH = Font.getDefaultFont().getHeight() * 5 / 2 + elementsPadding*2;
            if (this.h != H_AUTO && !this.trimHeight) { // TODO Make the same way in ButtonCol
                this.elemH = Math.max(this.elemH, this.h / elements.length);
            }
        } else {
            this.h = Math.min(this.h, this.elemH * elements.length);
        }
        
        if (this.h == H_AUTO) {
            this.h = elements.length * this.elemH;
        }
        
        if (this.trimHeight) {
            this.h = Math.min(this.h, this.elemH * elements.length);
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
    
    public int getTotalElemsH() {
        if (elements == null) {
            return 0;
        }
        
        return elements.length * getElemH();
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
        if (enableAnimations && scrollOffset != this.scrollOffset) {
            initAnimationThread();
            animationThread.animate(0, this.scrollOffset, 0, scrollOffset, 200, 0, 0, 0, elemH*elements.length - h);
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
        
        return elements[selected].pointerClicked(x, y);
    }
    
    public boolean handlePointerPressed(int x, int y) {
        if (!isVisible) {
            return false;
        }
        
        if (elements == null || elements.length == 0) {
            return false;
        }
        
        if (!isScrollable) {
            return false;
        }
        
        if (elemH*elements.length <= h) {
            //return false;
        }
        
        if (!checkTouchEvent(x, y)) {
            pointerPressedX = pointerPressedY = -1;
            return false;
        }
        
        pointerPressedX = x;
        pointerPressedY = y;
        scrollOffsetWhenPressed = scrollOffset;
        
        setSelected((y - y0 + scrollOffset) / elemH);
        
        elements[selected].pointerPressed(x, y);
        
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
        
        if (elemH*elements.length <= h) {
            //return false;
        }
        
        if (elements[selected].pointerDragged(x, y)) {
        	return true;
        }
        
        scrollOffset = scrollOffsetWhenPressed - (y - pointerPressedY);
        
        scrollOffset = Math.max(0, Math.min(scrollOffset, getTotalElemsH() - h));
        
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
               case Keys.UP:
                   if (selected > 0) {
                       setSelected(selected-1);
                   } else {
                   	setSelected(elements.length - 1);
                   }
                   break;
               case Keys.DOWN:
                   if (selected < elements.length - 1) {
                   	setSelected(selected+1);
                   } else {
                   	setSelected(0);
                   }
                   break;
               default:
               	return false;
           }
	   }
	   
	   int selectedH = elemH * selected;
	   int startY = scrollOffset;
	   int targetY = scrollOffset;
       int topLimitY = 0;
       int bottomLimitY = elemH*elements.length - h;
	   if (selectedH - elemH < scrollOffset) {
	       targetY = Math.max(topLimitY, selectedH - elemH * 3 / 4);
	   }
	   
	   if (selectedH + elemH > scrollOffset + h) {
	       targetY = Math.min(bottomLimitY, selectedH - h + elemH + elemH * 3 / 4);
	   }
	   
	   if (enableAnimations && targetY != startY) {
	       initAnimationThread();
	       animationThread.animate(0, startY, 0, targetY, 200, 0, 0, topLimitY, bottomLimitY);
	   } else {
	       scrollOffset = targetY;
	   }
	   
	   if (isSelectionEnabled) {
	       isSelectionVisible = true;
	   }
	   
	   return true;
   }
    
    public List enableScrolling(boolean isScrollable, boolean startFromBottom) {
        this.startFromBottom = startFromBottom;
        
        if (isScrollable) {
            setIsSelectionEnabled(true);
        }
        
        this.isScrollable = isScrollable;
        return this;
    }

    public List enableAnimations(boolean b) {
        enableAnimations = b;
        return this;
    }

    public List trimHeight(boolean b) {
        trimHeight = b;
        return this;
    }
    
    public void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
        if (elements == null || elements.length == 0) {
            return;
        }

        for (int i = 0; i < elements.length; i++) {
            Font prevFont = g.getFont();
            int elemH = this.elemH;
            int elemY = y0 - scrollOffset + i*elemH;

            if (elemY + elemH < y0) {
                continue;
            }
            
            if (elemY > y0 + h) {
                break;
            }
            
            boolean drawAsSelected = (i == selected && isSelectionVisible && isFocused);
            drawBgUnderElement(g, x0, elemY, w, elemH, !forceInactive, drawAsSelected);
            elements[i].paint(g, x0, elemY, w, elemH, forceInactive);
            g.setFont(prevFont);

            if (drawAsSelected) {
                int markY0 = elemH / 3;
                int markY1 = elemH - markY0;
                int markCenterY = (markY0 + markY1) / 2;
                int markw = (markY1 - markY0) / 2;
                g.fillTriangle(x0 + 1, elemY + markY0, x0 + 1, elemY + markY1, x0 + markw, elemY + markCenterY);
                g.fillTriangle(x0 + w - 1, elemY + markY0, x0 + w - 1, elemY + markY1, x0 + w - markw, elemY + markCenterY);
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
    
    public List setElements(IUIComponent[] elements) {
        this.elements = elements;
        if (!isInited || elements == null) {
        	return this;
        }
        
        for (int i = 0; i < elements.length; i++) {
			elements[i].setParent(this);
			elements[i].init();
			elements[i].setBgColor(COLOR_TRANSPARENT);
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
    
    public List setElementsBgColor(int color) {
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
    
    public List setElementsPadding(int padding) {
        elementsPadding = padding;
        if (elements == null) {
            return this;
        }
        
        for (int i = 0; i < elements.length; i++) {
            elements[i].setPadding(padding);
        }
        return this;
    }
    
    public List setIsSelectionEnabled(boolean selectionEnabled) {
        this.isSelectionEnabled = selectionEnabled;
        return this;
    }

    public List setIsSelectionVisible(boolean isSelectionVisible) {
        this.isSelectionVisible = isSelectionVisible;
        return this;
    }

    public List setSelected(int selected) {
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