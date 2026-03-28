// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.Logger;

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

    protected void setPageContentBounds(IUIComponent pageContent, int x0, int y0, int w, int h) {
        super.setPageContentBounds(pageContent, x0, y0, w, h);
        try {
            if (getUISettings().getTransparencyEnabled()) {
                setBgImage(((Container) parent).getBlurredCapture());
            } else {
                setBgImage(null);
            }
        } catch (Exception ex) {
            Logger.log(ex);
        }
    }

    protected void close() {
        feedback.closePopup();
    }

}
