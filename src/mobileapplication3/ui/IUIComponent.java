/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.ui;

import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public interface IUIComponent {
    public static final int
            TOP = Graphics.TOP,
            BOTTOM = Graphics.BOTTOM,
            LEFT = Graphics.LEFT,
            RIGHT = Graphics.RIGHT,
            HCENTER = Graphics.HCENTER,
            VCENTER = Graphics.VCENTER,
            COLOR_TRANSPARENT = -1,
            NOT_SET = -2,
            KEYCODE_LEFT_SOFT = -6,
            KEYCODE_RIGHT_SOFT = -7,
            COLOR_ACCENT = 0x000055,
            COLOR_ACCENT_MUTED = 0x101020,
            BG_COLOR_INACTIVE = 0x202025,
            BG_COLOR_WARN = 0x222200,
            BG_COLOR_DANGER = 0x550000,
            BG_COLOR_SELECTED = 0x003375,
            BG_COLOR_HIGHLIGHTED = 0x151535,
            FONT_COLOR_INACTIVE = 0x404040,
            FONT_COLOR = 0xffffff;
    public static final int
            MOUSE_PRIMARY_PRESSED = 1, MOUSE_PRIMARY_RELEASED = -1,
            MOUSE_SECONDARY_PRESSED = 2, MOUSE_SECONDARY_RELEASED = -2,
            MOUSE_WHEEL_PRESSED = 3, MOUSE_WHEEL_RELEASED = -3,
            MOUSE_PRIMARY_DRAGGED = 4, MOUSE_SECONDARY_DRAGGED = 5, MOUSE_WHEEL_DRAGGED = 6,
            MOUSE_WHEEL_SCROLLED_DOWN = 7, MOUSE_WHEEL_SCROLLED_UP = -7;

    public IUIComponent setParent(IContainer parent);
    public IUIComponent setPos(int x0, int y0, int anchor);
    public IUIComponent setSize(int w, int h);
    public IUIComponent setVisible(boolean b);
    public IUIComponent setFocused(boolean b);
    public IUIComponent setActive(boolean b);
    public IUIComponent setBgColor(int color);
    public IUIComponent setPadding(int padding);
    public IUIComponent roundBg(boolean b);
    public boolean getIsVisible();
    public boolean repaintOnlyOnFlushGraphics();
    public void paint(Graphics g);
    public void paint(Graphics g, int x0, int y0, int w, int h);
    public void paint(Graphics g, boolean forceInactive);
    public void paint(Graphics g, int x0, int y0, int w, int h, boolean forceInactive);
    public boolean canBeFocused();
    public boolean isFocused();
    public boolean pointerClicked(int x, int y);
    public boolean pointerReleased(int x, int y);
    public boolean pointerDragged(int x, int y);
    public boolean pointerPressed(int x, int y);
    public boolean keyPressed(int keyCode, int count);
    public boolean keyReleased(int keyCode, int count);
    public boolean keyRepeated(int keyCode, int pressedCount);
    public boolean mouseEvent(int event, int x, int y);
    public void onShow();
    public void onHide();
    public boolean checkTouchEvent(int x, int y);
    public void init();
    public void postInit();
}
