// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.ui.Graphics;

public abstract class Switch extends Button {

    public Switch(String title) {
        super(title);
    }

    protected void drawText(Graphics g, String text, int x0, int y0, int w, int h, boolean isSelected, boolean isFocused, boolean forceInactive, boolean showKbHints) {
        int switchH = font.getHeight();
        int switchW = switchH * 9 / 5;
        int switchCenterX = x0 + w - Math.min((h - switchH) / 2, switchH) - switchW / 2;
        int textSwitchSepW = Math.max(switchH, getBgPadding());

        // text
        int textAreaW = switchCenterX - switchW / 2 - textSwitchSepW - x0;
        int textCenterOffset = (w - textAreaW) / 2;
        if (textAreaW < switchW * 4) {
            textCenterOffset = 0;
        }
        super.drawText(g, text, x0, y0, textAreaW, h, textCenterOffset, 0, isSelected, isFocused, forceInactive, showKbHints);

        int switchX0 = switchCenterX - switchW / 2;
        int switchY0 = y0 + (h - switchH) / 2;

        int switchColor = isActive() ? fontColor : fontColorInactive;

        boolean value = getValue();

        // outline
        int outlineThickness = Math.max(1, font.getHeight() / 20);
        int outlineW = switchW + outlineThickness * 2;
        int outlineH = switchH + outlineThickness * 2;
        int outlineArcD = Math.min(outlineW, outlineH);
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
