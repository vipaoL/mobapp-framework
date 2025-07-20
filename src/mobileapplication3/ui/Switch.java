package mobileapplication3.ui;

import mobileapplication3.platform.ui.Graphics;

public abstract class Switch extends Button {
	private boolean value;
	int padding;
	int switchX0;

	public Switch(String title) {
		super(title);
		value = getValue();
	}
	
	protected void drawText(Graphics g, String text, int x0, int y0, int w, int h, boolean isSelected, boolean isFocused, boolean forceInactive, boolean showKbHints) {
		int switchH = h * 2 / 3;
		int switchCenterX = x0 + w - switchH * 3 / 2;
		int switchW = switchH * 5 / 2;

		padding = switchW / 8;
		super.drawText(g, text, x0, y0, switchCenterX - switchW / 2 - x0, h, isSelected, isFocused, forceInactive, showKbHints);
		
		switchX0 = switchCenterX - switchW / 2 + padding;
		switchW -= padding * 2;
		int switchY0 = y0 + (h - switchH) / 2;

		int switchColor = isActive() ? fontColor : fontColorInactive;
		int outlineThickness = Math.max(1, font.getHeight() / 10);
		g.fillRoundRect(switchX0 - outlineThickness, switchY0 - outlineThickness, switchW + outlineThickness * 2, switchH + outlineThickness * 2, switchW / 2  + outlineThickness * 2, switchH + outlineThickness * 2);

		if (isActive()) {
			if (value) {
				g.setColor(IUIComponent.COLOR_ACCENT);
			} else {
				g.setColor(bgColorInactive);
			}
		} else {
			g.setColor(bgColorInactive);
		}
		
		g.fillRoundRect(switchX0, switchY0, switchW, switchH, switchW / 2, switchH);
		
		g.setColor(switchColor);
		
		int d = switchH * 4 / 5;
		int gap = (switchH - d) / 2;
		int x = switchX0 + gap;
		if (value) {
			x += switchW - d - gap * 2;
		}
		g.fillArc(x, switchY0 + gap, d, d, 0, 360);
	}
	
	public void buttonPressed() {
		value = !value;
		setValue(value);
	}
	
	public abstract boolean getValue();
	public abstract void setValue(boolean value);

}
