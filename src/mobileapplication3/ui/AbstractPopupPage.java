/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.ui;

/**
 *
 * @author vipaol
 */
public abstract class AbstractPopupPage extends Page {
    protected IPopupFeedback feedback;

    public AbstractPopupPage(String title, IPopupFeedback parent) {
        super(title);
        this.feedback = parent;
    }

    public void init() {
        super.init();
        try {
            if (getUISettings().getTransparencyEnabled()) {
                setBgImage(((Container) parent).getBlurredCapture());
            } else {
                setBgImage(null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void close() {
        feedback.closePopup();
    }
    
}
