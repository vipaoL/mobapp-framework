// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

/**
 *
 * @author vipaol
 */
public abstract class AbstractPopupWindow extends Container {
    protected IPopupFeedback feedback;

    public AbstractPopupWindow(IPopupFeedback parent) {
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
