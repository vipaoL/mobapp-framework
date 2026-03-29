// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.Mathh;

public class GraphicsUtils {
    public static int dimColor(int color, int percent) {
        int r = getColorRedComponent(color) * percent / 100;
        int g = getColorGreenComponent(color) * percent / 100;
        int b = getColorBlueComponent(color) * percent / 100;
        r = Mathh.constrain(0, r, 255);
        g = Mathh.constrain(0, g, 255);
        b = Mathh.constrain(0, b, 255);
        return (r << 16) + (g << 8) + b;
    }

    private static int getColorRedComponent(int color) {
        return (color >> 16) & 0xff;
    }

    private static int getColorGreenComponent(int color) {
        return (color >> 8) & 0xff;
    }

    private static int getColorBlueComponent(int color) {
        return color & 0xff;
    }

    public static int blendColor(int c1, int c2, int k1, int k2) {
        int r1 = getColorRedComponent(c1);
        int r2 = getColorRedComponent(c2);
        int g1 = getColorGreenComponent(c1);
        int g2 = getColorGreenComponent(c2);
        int b1 = getColorBlueComponent(c1);
        int b2 = getColorBlueComponent(c2);

        int r = r1 + (r2 - r1) * k1 / k2;
        int g = g1 + (g2 - g1) * k1 / k2;
        int b = b1 + (b2 - b1) * k1 / k2;

        return (r << 16) | (g << 8) | b;
    }

    public static double getLuma(int color) {
        int r = getColorRedComponent(color);
        int g = getColorGreenComponent(color);
        int b = getColorBlueComponent(color);

        return (0.299 * r) + (0.587 * g) + (0.114 * b);
    }
}
