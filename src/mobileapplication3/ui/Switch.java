// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;

public abstract class Switch extends Button {

    public Switch(String title) {
        super(title);
    }

    protected void drawText(Graphics g, String text, int x0, int y0, int w, int h, boolean isSelected, boolean isFocused, boolean forceInactive, boolean showKbHints) {
        int switchH = Font.getDefaultFontHeight();
        int switchW = switchH * 9 / 5;

        int outlineThickness = Math.max(1, switchH / 20);
        int outlineW = switchW + outlineThickness * 2;
        int outlineH = switchH + outlineThickness * 2;
        int outlineArcD = Math.min(outlineW, outlineH);

        int padding = Math.min(h - switchH, switchH) / 2;

        boolean verticalLayout = h > w * 0.6;

        int textAreaW, textAreaH, textX, textY, textCenterOffset;
        int switchX0, switchY0;

        if (verticalLayout) {
            textAreaW = w - padding * 2;
            textAreaH = h - outlineH - padding * 3;
            textX = x0 + padding;
            textY = y0 + padding;
            textCenterOffset = 0;

            switchX0 = x0 + (w - switchW) / 2;
            switchY0 = y0 + h - outlineH - padding;
        } else {
            switchX0 = x0 + w - padding - switchW;
            switchY0 = y0 + (h - outlineH) / 2;

            textAreaW = switchX0 - padding - x0;
            textAreaH = h;
            textX = x0;
            textY = y0;

            textCenterOffset = (w - textAreaW) / 2;
            if (textAreaW < switchW * 4) {
                textCenterOffset = 0;
            }
        }

        super.drawText(g, text, textX, textY, textAreaW, textAreaH, textCenterOffset, 0, isSelected, isFocused, forceInactive, showKbHints);

        int switchColor = isActive() ? fontColor : fontColorInactive;

        boolean value = getValue();

        g.setColor(switchColor);
        g.fillRoundRect(
                switchX0 - outlineThickness,
                switchY0 - outlineThickness,
                outlineW,
                outlineH,
                outlineArcD,
                outlineArcD
        );

        // bg
        int bgArcD = Math.min(switchW, switchH);
        g.setColor(value && isActive() ? IUIComponent.COLOR_ACCENT : bgColorInactive);
        g.fillRoundRect(switchX0, switchY0, switchW, switchH, bgArcD, bgArcD);

        // circle
        int d = switchH * 4 / 5;
        int gap = (switchH - d) / 2;
        int x = switchX0 + gap;
        if (value) {
            x += switchW - d - gap * 2;
        }
        g.setColor(switchColor);
        g.fillArc(x, switchY0 + gap, d, d, 0, 360);
    }

    public void buttonPressed() {
        setValue(!getValue());
    }

    public abstract boolean getValue();
    public abstract void setValue(boolean value);

}
