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

    public static void RGBToHSV(int color, float[] hsv) {
        float r = getColorRedComponent(color) / 255.0f;
        float g = getColorGreenComponent(color) / 255.0f;
        float b = getColorBlueComponent(color) / 255.0f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0;
        if (delta != 0) {
            if (max == r) {
                h = (g - b) / delta;
            } else if (max == g) {
                h = (b - r) / delta + 2f;
            } else {
                h = (r - g) / delta + 4f;
            }
        }
        h *= 60;
        if (h < 0) {
            h += 360;
        }

        hsv[0] = h;
        hsv[1] = (max == 0) ? 0 : (delta / max);
        hsv[2] = max;
    }

    public static int HSVToRGB(float h, float s, float v) {
        while (h < 0) {
            h += 360;
        }
        h = h % 360;

        float c = v * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = v - c;

        float r1 = 0;
        float g1 = 0;
        float b1 = 0;

        if (h < 60) {
            r1 = c;
            g1 = x;
            b1 = 0;
        } else if (h < 120) {
            r1 = x;
            g1 = c;
            b1 = 0;
        } else if (h < 180) {
            r1 = 0;
            g1 = c;
            b1 = x;
        } else if (h < 240) {
            r1 = 0;
            g1 = x;
            b1 = c;
        } else if (h < 300) {
            r1 = x;
            g1 = 0;
            b1 = c;
        } else {
            r1 = c;
            g1 = 0;
            b1 = x;
        }

        int r = (int) ((r1 + m) * 255f + 0.5f);
        int g = (int) ((g1 + m) * 255f + 0.5f);
        int b = (int) ((b1 + m) * 255f + 0.5f);

        return (Mathh.constrain(0, r, 255) << 16) |
                (Mathh.constrain(0, g, 255) << 8) |
                Mathh.constrain(0, b, 255);
    }

    public static int shiftHue(int color, float degrees) {
        float[] hsv = new float[3];
        RGBToHSV(color, hsv);
        return HSVToRGB(hsv[0] + degrees, hsv[1], hsv[2]);
    }
}
