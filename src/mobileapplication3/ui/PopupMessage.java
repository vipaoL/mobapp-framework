// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.RootContainer;

/**
 *
 * @author vipaol
 */
public class PopupMessage extends AbstractPopupPage {
    private final String message;
    private String buttonText = "OK";

    public PopupMessage(String title, String message, final IUIComponent previousComponent) {
        super(title != null ? title : "", new IPopupFeedback() {
            public void closePopup() {
                if (previousComponent != null) {
                    RootContainer.setRootUIComponent(previousComponent);
                } else {
                    Platform.exit();
                }
            }
        });
        if (previousComponent == null) {
            buttonText = "Exit";
        }
        this.message = message != null ? message : "No message";
    }

    public PopupMessage(String title, String message, final IPopupFeedback feedback) {
        super(title, feedback);
        this.message = message;
    }

    protected Button[] getActionButtons() {
        return new Button[]{
            new Button(buttonText) {
                public void buttonPressed() {
                    feedback.closePopup();
                }
            }
        };
    }

    protected IUIComponent initAndGetPageContent() {
        // Hack for scrolling. TODO
        List list = new List();
        TextComponent text = new TextComponent(message);
        text.setTextAlignment(TOP | LEFT);
        list.setElements(new IUIComponent[]{text});
        list.setElemH(Font.getDefaultFontHeight() * message.length() / 10);
        return list;
    }

}
