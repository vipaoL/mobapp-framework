package mobileapplication3.ui;

import mobileapplication3.platform.Mathh;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;

public class Slider extends Container {

	private TextComponent title;
	private TextComponent valueIndicator;
	private ButtonRow buttonRow;
	private Property prop;

	private short value;
	private short minValue;
	private short maxValue;
	private int prevDraggedX;
	private long prevDraggedTime = 0;

	public Slider(Property prop) {
		minValue = prop.getMinValue();
		maxValue = prop.getMaxValue();

		title = new TextComponent(prop.getName());
		title.setBgColor(COLOR_TRANSPARENT);

		value = prop.getValue();
		valueIndicator = new TextComponent(String.valueOf(value));
		valueIndicator.setBgColor(COLOR_TRANSPARENT);

		buttonRow = new ButtonRow();
		buttonRow.setBgColor(COLOR_TRANSPARENT);
		buttonRow.setButtonsBgColor(COLOR_TRANSPARENT);
		buttonRow.setButtonsBgColorInactive(COLOR_TRANSPARENT);
		buttonRow.setButtons(new Button[] {
				new Button("-") {
					public void buttonPressed() {
						setValue((short) (getValue() - 1));
					}
				},
				new Button("+") {
					public void buttonPressed() {
						setValue((short) (getValue() + 1));
					}
				}
		});

		setBgColor(COLOR_ACCENT);
		setActive(!prop.isCalculated());
		roundBg(true);

		this.prop = prop;
	}

	public void init() {
		setComponents(new IUIComponent[] {title, valueIndicator, buttonRow});
	}

	protected void drawBg(Graphics g, int x0, int y0, int w, int h, boolean forceInactive) {
		boolean isActive = this.isActive && !forceInactive;

		int prevClipX = g.getClipX();
        int prevClipY = g.getClipY();
        int prevClipW = g.getClipWidth();
        int prevClipH = g.getClipHeight();

        int sliderFilledW = w * (value - minValue) / (maxValue - minValue);
        int roundingD = Math.min(w/5, h/5);

        // background
        if (isActive) {
        	g.setColor(COLOR_ACCENT_MUTED);
        } else {
        	g.setColor(BG_COLOR_INACTIVE);
        }

		int clipX = x0, clipY = y0, clipW = w, clipH = h, clipX2 = clipX + clipW, clipY2 = clipY + clipH;
		clipX = Math.max(clipX, prevClipX);
		clipY = Math.max(clipY, prevClipY);
		clipX2 = Math.min(clipX2, prevClipX + prevClipW);
		clipY2 = Math.min(clipY2, prevClipY + prevClipH);
		clipW = Math.max(0, clipX2 - clipX);
		clipH = Math.max(0, clipY2 - clipY);
		g.setClip(clipX, clipY, clipW, clipH);
        g.fillRoundRect(x0, y0, w, h, roundingD, roundingD);

        // slider
        if (isActive) {
        	g.setColor(COLOR_ACCENT);
        } else {
        	g.setColor(COLOR_ACCENT_MUTED);
        }

		clipX = x0;
		clipY = y0;
		clipW = sliderFilledW;
		clipH = h;
		clipX2 = clipX + clipW;
		clipY2 = clipY + clipH;
		clipX = Math.max(clipX, prevClipX);
		clipY = Math.max(clipY, prevClipY);
		clipX2 = Math.min(clipX2, prevClipX + prevClipW);
		clipY2 = Math.min(clipY2, prevClipY + prevClipH);
		clipW = Math.max(0, clipX2 - clipX);
		clipH = Math.max(0, clipY2 - clipY);
		g.setClip(clipX, clipY, clipW, clipH);
        g.fillRoundRect(x0, y0, w, h, roundingD, roundingD);
	
		g.setClip(prevClipX, prevClipY, prevClipW, prevClipH);
	}

	protected void onSetBounds(int x0, int y0, int w, int h) {
		buttonRow
			.setSize(ButtonRow.W_AUTO, h/2)
			.setPos(x0 + w, y0 + h, BOTTOM | RIGHT);
		valueIndicator
			.setSize(w - buttonRow.w, buttonRow.h)
			.setPos(x0, y0 + h, BOTTOM | LEFT);
		title
			.setSize(w, h - buttonRow.h)
			.setPos(x0, y0, TOP | LEFT);
	}

	public short getValue() {
		return prop.getValue();
	}

	public void setValue(int value) {
		this.value = (short) Mathh.constrain(minValue, value, maxValue);
		prop.setValue(this.value);
		valueIndicator.setText(String.valueOf(this.value));
	}

	public boolean pointerPressed(int x, int y) {
		prevDraggedX = x;
		prevDraggedTime = System.currentTimeMillis();
		return super.pointerPressed(x, y);
	}

	public boolean pointerDragged(int x, int y) {
		int dx = x - pressedX;
		int dy = y - pressedY;
		int dt = (int) (System.currentTimeMillis() - prevDraggedTime);

		if (Math.abs(dx) < Math.abs(dy * 5)) {
			prevDraggedX = x;
			// list scrolling handles the pointer event if false is returned
			// so scroll the list if the vertical movement is greater than the horizontal movement
			return false;
		}

		if (dt != 0) {
			prevDraggedTime = System.currentTimeMillis();

			dx = Mathh.constrain(-200, x - prevDraggedX, 200); // prevent int overflow
			int prevValue = value;
			setValue((value + dx * dx * dx / dt / w));

			if (value != prevValue) {
				// Do not change prevDraggedX on small movements to allow set more precisely.
				// On the next pointerDragged event, dx will be greater
				prevDraggedX = x;
			}
		}

		return true;
	}

	public boolean keyPressed(int keyCode, int count) {
		if (!isActive || !isVisible) {
            return false;
        }

		switch (RootContainer.getAction(keyCode)) {
            case Keys.RIGHT:
            	setValue(value + count * count);
                return true;
            case Keys.LEFT:
            	setValue(value - count * count);
                return true;
            default:
            	return super.keyPressed(keyCode, count);
        }
	}

	public boolean keyRepeated(int keyCode, int pressedCount) {
		if (!isActive || !isVisible) {
            return false;
        }

		switch (RootContainer.getAction(keyCode)) {
            case Keys.RIGHT:
            	setValue(value + pressedCount * pressedCount);
                return true;
            case Keys.LEFT:
            	setValue(value - pressedCount * pressedCount);
                return true;
            default:
            	return super.keyRepeated(keyCode, pressedCount);
        }
	}

	public boolean canBeFocused() {
		return true;
	}

}
