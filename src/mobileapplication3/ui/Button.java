/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.ui;

import mobileapplication3.platform.Utils;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public abstract class Button {

	private String text;
    private String kbHint = "";
    private boolean isActive = true;
    protected int bgColor;
    protected int bgColorInactive;
    protected int fontColor;
    protected int fontColorInactive;
    protected int selectedBgColor;
    private int bgPadding;
    
    private int[][] lineBounds = null;
    private int prevW;
    public Font font;
    private Font prevGetLineBoundsFont;
    private boolean wasKbHintVisible = false;
    
    private int[] bindedKeyCodes = null;
    
    public Button(String title) {
        setFont(Font.getDefaultFontSize());
        this.bgPadding = 0;
        this.selectedBgColor = IUIComponent.BG_COLOR_SELECTED;
        this.fontColorInactive = IUIComponent.FONT_COLOR_INACTIVE;
        this.fontColor = IUIComponent.FONT_COLOR;
        this.bgColorInactive = IUIComponent.BG_COLOR_INACTIVE;
        this.bgColor = IUIComponent.COLOR_ACCENT_MUTED;
        
        this.text = title;
        setTitle(getTitle());
    }
    
    public boolean invokePressed(boolean isSelected, boolean isFocused) {
        if (isActive) {
            if (!isSelected) {
                buttonPressed();
            } else {
                buttonPressedSelected();
            }
            setTitle(getTitle());
            return true;
        }
        
        return false;
    }
    
    protected void setFont(int size, Graphics g) {
    	setFont(size);
    	g.setFont(font);
	}
    
    public void setFont(int size) {
		font = new Font(size);
	}
    
    public Button setIsActive(boolean b) {
        isActive = b;
        return this;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public Button setFontColor(int fontColor) {
		this.fontColor = fontColor;
		return this;
	}
    
    public int getFontColor() {
		return fontColor;
	}
    
    public Button setFontColorInactive(int fontColorInactive) {
		this.fontColorInactive = fontColorInactive;
		return this;
	}
    
    public int getFontColorInactive() {
		return fontColorInactive;
	}
    
    public int getBgColor() {
        return bgColor;
    }

    public Button setBgColor(int bgColor) {
        this.bgColor = bgColor;
        return this;
    }
    
    public int getBgColorInactive() {
		return bgColorInactive;
	}
    
    public Button setBgColorInactive(int bgColorInactive) {
    	this.bgColorInactive = bgColorInactive;
        return this;
    }
    
    public int getSelectedColor() {
        return selectedBgColor;
    }

    public Button setSelectedColor(int selectedColor) {
        this.selectedBgColor = selectedColor;
        return this;
    }
    
    public int getBgPagging() {
        return bgPadding;
    }

    public Button setBgPadding(int bgPadding) {
        this.bgPadding = bgPadding;
        return this;
    }
    
    public Button setTitle(String s) {
        if (s == null) {
            s = "<null>";
        }
        text = s;
        return this;
    }
    
    public String getTitle() {
        return text;
    }
    
    public String toString() {
        return text;
    }
    
    private void setKbHint(String s) {
        if (s == null) {
            s = "";
        }
        kbHint = s;
    }
    
    public Button setBindedKeyCode(int keyCode) {
    	return setBindedKeyCodes(new int[] {keyCode});
    }
    
    public Button setBindedKeyCodes(int[] keyCodes) {
    	if (keyCodes == null || keyCodes.length == 0) {
    		resetKeyBinds();
    		return this;
    	}

    	bindedKeyCodes = keyCodes;
        StringBuffer hintStringBuffer = new StringBuffer();
    	for (int i = 0; i < keyCodes.length; i++) {
            int keyCode = keyCodes[i];
            if (keyCode != Keys.KEY_SOFT_LEFT && keyCode != Keys.KEY_SOFT_RIGHT) {
                hintStringBuffer.append(",").append(Keys.getButtonName(keyCode));
            }
		}
        String hint = hintStringBuffer.toString();
        if (hint.length() > 0) {
            setKbHint("(" + hint.substring(1) + ")");
        }
    	return this;
    }
    
    public void resetKeyBinds() {
    	bindedKeyCodes = new int[] {};
		setKbHint(null);
    }
    
    public int[] getBindedKeyCodes() {
    	return bindedKeyCodes;
    }

    public void paint(Graphics g, int x0, int y0, int w, int h, boolean isSelected, boolean isFocused, boolean drawAsInactive, boolean kbHintVisible) {
    	paint(g, x0, y0, w, h, x0, y0, w, h, isSelected, isFocused, drawAsInactive, kbHintVisible);
    }
    
    public void paint(Graphics g, int x0, int y0, int w, int h, int clipX, int clipY, int clipW, int clipH, boolean isSelected, boolean isFocused, boolean drawAsInactive, boolean kbHintVisible) {
        int prevClipX = g.getClipX();
        int prevClipY = g.getClipY();
        int prevClipW = g.getClipWidth();
        int prevClipH = g.getClipHeight();
        
        x0 += bgPadding;
        clipX += bgPadding;
        y0 += bgPadding;
        w -= bgPadding*2;
        clipW -= bgPadding*2;
        h -= bgPadding*2;
        if (w <= 0 || h <= 0) {
        	return;
        }

        int clipX2 = clipX + clipW, clipY2 = clipY + clipH;
        clipX = Math.max(clipX, prevClipX);
        clipY = Math.max(clipY, prevClipY);
        clipX2 = Math.min(clipX2, prevClipX + prevClipW);
        clipY2 = Math.min(clipY2, prevClipY + prevClipH);
        clipW = Math.max(0, clipX2 - clipX);
        clipH = Math.max(0, clipY2 - clipY);
        g.setClip(clipX, clipY, clipW, clipH);
        
        drawBg(g, clipX, clipY, clipW, clipH, isSelected, drawAsInactive);
        drawText(g, text, x0, y0, w, h, isSelected, isFocused, drawAsInactive, kbHintVisible);
        drawSelectionMark(g, clipX, clipY, clipW, clipH, isSelected, isFocused, drawAsInactive);
        
        g.setClip(prevClipX, prevClipY, prevClipW, prevClipH);
    }
    
    protected void drawSelectionMark(Graphics g, int x0, int y0, int w, int h, boolean isSelected, boolean isFocused, boolean forceInactive) {
    	if (isSelected) {
            g.setColor(getCurrentFontColor(forceInactive));
            int markY0 = h / 3;
            int markY1 = h - markY0;
            int markCenterY = (markY0 + markY1) / 2;
            int markw = (markY1 - markY0) / 2;
            g.fillTriangle(x0 + 1, y0 + markY0, x0 + 1, y0 + markY1, x0 + markw, y0 + markCenterY);
            g.fillTriangle(x0 + w - 1, y0 + markY0, x0 + w - 1, y0 + markY1, x0 + w - markw, y0 + markCenterY);
        }
	}
    
    protected void drawText(Graphics g, String text, int x0, int y0, int w, int h, boolean isSelected, boolean isFocused, boolean forceInactive, boolean showKbHints) {
    	Font prevFont = g.getFont();
        setFont(Font.getDefaultFontSize());
        
        if (showKbHints) {
        	text += kbHint;
        }
        
        int[][] lineBounds = getLineBounds(text, font, w, bgPadding, showKbHints);
        if (h / lineBounds.length < font.getHeight()) {
        	setFont(Font.SIZE_MEDIUM);
        	lineBounds = getLineBounds(text, font, w, bgPadding, showKbHints);
        	if (h / lineBounds.length < font.getHeight()) {
            	setFont(Font.SIZE_SMALL);
            	lineBounds = getLineBounds(text, font, w, bgPadding, showKbHints);
            }
        }
        setFont(font.getSize(), g);

        g.setColor(getCurrentFontColor(forceInactive));
        
        int offset = 0;
        int step = font.getHeight() * 3 / 2;
        if (step * lineBounds.length > h - bgPadding * 2) {
        	step = h / (lineBounds.length);
        }
        
        offset += (h-step*(lineBounds.length - 1) - font.getHeight())/2;
        for (int i = 0; i < lineBounds.length; i++) {
            int[] bounds = lineBounds[i];
            g.drawSubstring(text, bounds[0], bounds[1], x0 + w/2, y0 + offset, Graphics.HCENTER | Graphics.TOP);
            offset += step;
        }
        
        g.setFont(prevFont);
	}
    
    protected void drawBg(Graphics g, int x0, int y0, int w, int h, boolean isSelected, boolean forceInactive) {
    	int r = Math.min(w/5, h/5);
        
        int bgColor;
        if (isActive && !forceInactive) {
        	if (!isSelected) {
        		bgColor = this.bgColor;
        	} else {
        		bgColor = selectedBgColor;
        	}
        } else {
        	bgColor = bgColorInactive;
        }
        
        if (bgColor > 0) {
            g.setColor(bgColor);
            //g.fillRect(x0, y0, w, h); // TODO add feature to disable rouding
            g.fillRoundRect(x0, y0, w, h, r, r);
        }
    }
    
    protected int getCurrentFontColor(boolean forceInactive) {
        if (isActive && !forceInactive) {
        	return this.fontColor;
        } else {
        	return fontColorInactive;
        }
    }
    
    private int[][] getLineBounds(String text, Font font, int w, int padding, boolean kbHintVisible) {
        if (lineBounds != null && w == prevW && font.getSize() == prevGetLineBoundsFont.getSize() && kbHintVisible == wasKbHintVisible) {
            return lineBounds;
        }
        
        prevW = w;
        
        lineBounds = font.getLineBounds(text, w, padding);
        prevGetLineBoundsFont = font;
        return lineBounds;
    }
    
    public int getMinPossibleWidth() {
        int w = 0;
        String[] words = Utils.split(text, " ");
        for (int i = 0; i < words.length; i++) {
            w = Math.max(w, font.stringWidth(words[i]));
        }
        return w;
    }
    
    public abstract void buttonPressed();
    public void buttonPressedSelected() {
        buttonPressed();
    }
    
}
