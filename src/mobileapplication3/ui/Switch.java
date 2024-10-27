package mobileapplication3.ui;

import mobileapplication3.platform.ui.Graphics;

public abstract class Switch extends Button {
	
	private boolean value;
	int padding;
	private int switchW;
	int switchX0;
	protected boolean showKbHints;

	public Switch(String title) {
		super(title);
		value = getValue();
	}
	
	protected void drawText(Graphics g, String text, int x0, int y0, int w, int h, boolean isSelected, boolean isFocused, boolean forceInactive, boolean showKbHints) {
		int switchFreeSpace = w / 4;
		int switchCenterX = x0 + w * 7 / 8;
		int switchH = Math.min(switchFreeSpace / 2, h * 2 / 3);
		switchW = Math.min(switchFreeSpace, switchH * 3);
		
		padding = switchW / 8;
		super.drawText(g, text, x0, y0, switchCenterX - switchW / 2 - x0, h, isSelected, isFocused, forceInactive, showKbHints);
		
		switchX0 = switchCenterX - switchW / 2 + padding;
		switchW -= padding * 2;
		int switchY0 = y0 + (h - switchH) / 2;
		
		if (isActive()) {
			if (value) {
				g.setColor(IUIComponent.COLOR_ACCENT);
			} else {
				g.setColor(IUIComponent.BG_COLOR_INACTIVE);
			}
		} else {
			g.setColor(bgColorInactive);
		}
		
		g.fillRoundRect(switchX0, switchY0, switchW, switchH, switchW / 2, switchH);
		
		if (isActive()) {
			g.setColor(fontColor);
		} else {
			g.setColor(fontColorInactive);
		}
		
		int d = switchH * 4 / 5;
		int x = switchX0;
		if (value) {
			x += switchW - d;
		}
		g.fillArc(x, switchY0 + (switchH - d) / 2, d, d, 0, 360);
	}
	
	public void buttonPressed() {
		value = !value;
		setValue(value);
	}
	
	public abstract boolean getValue();
	public abstract void setValue(boolean value);

}
