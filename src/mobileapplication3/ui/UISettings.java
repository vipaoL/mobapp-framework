// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

public interface UISettings {
    public abstract boolean getKbSmoothScrollingEnabled();
    public abstract boolean getKineticTouchScrollingEnabled();
    public abstract boolean getTransparencyEnabled();
    public abstract boolean getKeyRepeatedInListsEnabled();
    public abstract boolean showKbHints();
    public abstract boolean enableOnScreenLog();
    public abstract void onChange();
}
