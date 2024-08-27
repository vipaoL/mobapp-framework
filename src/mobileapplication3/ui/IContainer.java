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
public interface IContainer {
    public void repaint();
    public Graphics getUGraphics();
    public void flushGraphics();
	public UISettings getUISettings();
}
