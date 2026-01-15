// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public interface IContainer {
    public void repaint();
    public Graphics getUGraphics();
    public void flushGraphics();
    public UISettings getUISettings();
    public boolean isOnScreen();
}
