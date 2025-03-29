package mobileapplication3.ui;

import mobileapplication3.platform.ui.RootContainerView;

public class BackButton extends Button {
	IPopupFeedback windowParent;

	public BackButton(IPopupFeedback windowParent) {
		super(windowParent instanceof RootContainerView ? "Exit" : "Back");
		this.windowParent = windowParent;
		setBindedKeyCodes(new int[]{Keys.KEY_NUM0});
		if (windowParent instanceof RootContainerView) {
			setBgColor(IUIComponent.BG_COLOR_DANGER);
		}
	}

	public void buttonPressed() {
		windowParent.closePopup();
	}

}
