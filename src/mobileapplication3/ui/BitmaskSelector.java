// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

public class BitmaskSelector extends Container {
    private final TextComponent title;
    private final ButtonRow buttonRow;

    public BitmaskSelector(final BitmaskProperty prop) {
        title = new TextComponent(prop.getName());
        title.setBgColor(COLOR_TRANSPARENT);

        int bits = prop.getBitsCount();
        Button[] buttons = new Button[bits];
        for (int i = 0; i < bits; i++) {
            final int bitIndex = i;
            buttons[i] = new Button(String.valueOf(bitIndex)) {
                public void buttonPressed() {
                    int val = prop.getValue();
                    val ^= (1 << bitIndex);
                    prop.setValue(val);
                }

                public int getBgColor() {
                    return (prop.getValue() & (1 << bitIndex)) != 0 ? BG_COLOR_SELECTED : COLOR_ACCENT_MUTED;
                }

                public int getBgColorSelected() {
                    return getBgColor();
                }
            };
        }

        buttonRow = new ButtonRow(buttons);
        buttonRow.setBgColor(COLOR_TRANSPARENT);
        buttonRow.setIsSelectionEnabled(true);
        buttonRow.enableScrolling(true);

        setBgColor(COLOR_ACCENT);
        setActive(prop.isActive());
        roundBg(true);
    }

    public void init() {
        setComponents(new IUIComponent[] {title, buttonRow});
    }

    public boolean canBeFocused() {
        return true;
    }

    protected void onSetBounds(int x0, int y0, int w, int h) {
        title
            .setSize(w, h / 2)
            .setPos(x0, y0, TOP | LEFT);
        buttonRow
            .setSize(w, h / 2)
            .setPos(x0, y0 + h / 2, TOP | LEFT);
    }
}
