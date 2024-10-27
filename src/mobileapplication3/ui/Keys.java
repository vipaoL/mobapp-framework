package mobileapplication3.ui;

public class Keys {
	public static final int
		KEY_SOFT_LEFT = -6,
	    KEY_SOFT_RIGHT = -7,
		KEY_UP = -1,
		KEY_DOWN = -2,
		KEY_LEFT = -3,
		KEY_RIGHT = -4,
		KEY_CENTER = -5,
		KEY_NUM0 = 48,
		KEY_NUM1 = 49,
		KEY_NUM2 = 50,
		KEY_NUM3 = 51,
		KEY_NUM4 = 52,
		KEY_NUM5 = 53,
		KEY_NUM6 = 54,
		KEY_NUM7 = 55,
		KEY_NUM8 = 56,
		KEY_NUM9 = 57,
		KEY_STAR = 42,
		KEY_POUND = 35,
		UP = 1,
		DOWN = 6,
		LEFT = 2,
		RIGHT = 5,
		FIRE = 8,
		GAME_A = 9,
		GAME_B = 10,
		GAME_C = 11,
		GAME_D = 12;

	public static final String getButtonName(int keyCode) {
		switch (keyCode) {
		case KEY_SOFT_LEFT:
			return "LSoft";
		case KEY_SOFT_RIGHT:
			return "RSoft";
		case UP:
			return "Up";
		case DOWN:
			return "Down";
		case LEFT:
			return "Left";
		case RIGHT:
			return "Right";
		case FIRE:
			return "Fire";
		case GAME_A:
			return "gA";
		case GAME_B:
			return "gB";
		case GAME_C:
			return "gC";
		case GAME_D:
			return "gD";
		case KEY_NUM0:
			return "0";
		case KEY_NUM1:
			return "1";
		case KEY_NUM2:
			return "2";
		case KEY_NUM3:
			return "3";
		case KEY_NUM4:
			return "4";
		case KEY_NUM5:
			return "5";
		case KEY_NUM6:
			return "6";
		case KEY_NUM7:
			return "7";
		case KEY_NUM8:
			return "8";
		case KEY_NUM9:
			return "9";
		case KEY_STAR:
			return "*";
		case KEY_POUND:
			return "#";
		default:
			return "?";
		}
	}
}
