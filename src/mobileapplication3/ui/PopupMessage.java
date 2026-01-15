// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

/**
 *
 * @author vipaol
 */
public class PopupMessage extends AbstractPopupPage {

    private String message;

    public PopupMessage(String title, String message, final IPopupFeedback feedback) {
        super(title, feedback);
        this.message = message;
    }

    protected Button[] getActionButtons() {
        return new Button[]{
            new Button("OK") {
                public void buttonPressed() {
                    feedback.closePopup();
                }
            }
        };
    }

    protected IUIComponent initAndGetPageContent() {
        return new TextComponent(message);
    }

}
