/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.ui;

/**
 *
 * @author vipaol
 */
public abstract class AbstractButtonSet extends UIComponent {
    
    public static final int W_AUTO = -1;
    public static final int H_AUTO = -1;
    
    public Button[] buttons = null;
    protected int bgColor = COLOR_TRANSPARENT;
    protected int buttonsBgColor = NOT_SET;
    protected int buttonsBgColorInactive = NOT_SET;
    protected int buttonsSelectedColor = NOT_SET;
    protected int buttonsBgPadding = 0;
    
    protected int selected = 0;
    protected int prevSelected = 0;
    protected boolean isSelectionEnabled = false;
    protected boolean isSelectionVisible = false;
    protected boolean ignoreKeyRepeated = true;
    protected boolean showKbHints;
    
    public void init() {
    	try {
    		ignoreKeyRepeated = !getUISettings().getKeyRepeatedInListsEnabled();
    		showKbHints = getUISettings().showKbHints();
    		isSelectionVisible = isSelectionVisible || showKbHints;
    	} catch (Exception ex) { }
    }

    public AbstractButtonSet setButtons(Button[] buttons) {
        this.buttons = buttons;
        
        setButtonsBgColor(buttonsBgColor);
        setButtonsBgColorInactive(buttonsBgColorInactive);
        setSelectedColor(buttonsSelectedColor);
        setButtonsBgPadding(buttonsBgPadding);
        setIsSelectionEnabled(isSelectionEnabled);
        setIsSelectionVisible(isSelectionVisible);
        
        if (isSizeSet()) {
            recalcSize();
        }
        
        return this;
    }
    
    public AbstractButtonSet setButtonsBgColor(int color) {
        if (color == NOT_SET) {
            return this;
        }
        
        this.buttonsBgColor = color;
        if (buttons == null) {
            return this;
        }
        
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setBgColor(color);
        }
        return this;
    }
    
    public AbstractButtonSet setButtonsBgColorInactive(int color) {
        if (color == NOT_SET) {
            return this;
        }
        
        this.buttonsBgColorInactive = color;
        if (buttons == null) {
            return this;
        }
        
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setBgColorInactive(color);
        }
        return this;
    }
    
    public AbstractButtonSet setSelectedColor(int color) {
        if (color == NOT_SET) {
            return this;
        }
        
        this.buttonsSelectedColor = color;
        if (buttons == null) {
            return this;
        }
        
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setSelectedColor(color);
        }
        return this;
    }
    
    public AbstractButtonSet setButtonsBgPadding(int padding) {
        buttonsBgPadding = padding;
        if (buttons == null) {
            return this;
        }
        
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setBgPadding(padding);
        }
        return this;
    }
    
    public AbstractButtonSet setIsSelectionEnabled(boolean selectionEnabled) {
        this.isSelectionEnabled = selectionEnabled;
        return this;
    }

    public AbstractButtonSet setIsSelectionVisible(boolean isSelectionVisible) {
        this.isSelectionVisible = isSelectionVisible;
        return this;
    }

    public AbstractButtonSet setSelected(int selected) {
        // TODO add check
        this.selected = selected;
        return this;
    }

    public int getSelected() {
        return selected;
    }

    public int getButtonCount() {
        if (buttons != null) {
            return buttons.length;
        } else {
            return 0;
        }
    }

    public boolean canBeFocused() {
        if (buttons == null || !isSelectionEnabled) {
            return false;
        }
        
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null) {
                if (buttons[i].isActive()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean handleKeyRepeated(int keyCode, int pressedCount) {
    	if (ignoreKeyRepeated) {
    		return isSelectionEnabled && isFocused && isVisible;
    	}

    	if (onKeyPressed(keyCode, pressedCount)) {
    		return true;
    	}
    	return false;
    }
    
    protected final boolean handleKeyPressed(int keyCode, int count) {
    	if (handleBindsOnKeyPressed(keyCode)) {
    		return true;
    	}
    	if (onKeyPressed(keyCode, count)) {
    		return true;
    	}
    	return isFocused;
    }
    
    protected boolean onKeyPressed(int keyCode, int count) {
    	return false;
    }
    
    protected boolean handleBindsOnKeyPressed(int keyCode) {
    	for (int i = 0; i < buttons.length; i++) {
    		Button button = buttons[i];
    		if (button.isActive()) {
	    		int[] binds = button.getBindedKeyCodes();
	    		if (binds == null) {
	    			continue;
	    		}
	
	    		for (int j = 0; j < binds.length; j++) {
	    			if (keyCode == binds[j]) {
	    				button.buttonPressed();
	    				return true;
	    			}
	    		}
    		}
    	}
    	return false;
    }
    
    public abstract int getMinPossibleWidth();
}
