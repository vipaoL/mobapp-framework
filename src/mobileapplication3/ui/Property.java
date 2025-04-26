package mobileapplication3.ui;

public class Property {
	private String name;
	private boolean isImmutable;
	private short value;

	public Property(String name, boolean isCalculatedAutomatically) {
		isImmutable = isCalculatedAutomatically;
		if (isImmutable) {
			name += " (calculated)";
		}
		this.name = name;
	}

	public Property(String name) {
		this(name, false);
	}

	public final String getName() {
		return name;
	}

	public short getMaxValue() {
		return Short.MAX_VALUE;
	}

	public short getMinValue() {
		return Short.MIN_VALUE;
	}

	public final boolean isCalculated() {
		return isImmutable;
	}

	public short getValue() {
		return value;
	}

	public void setValue(short value) {
		this.value = value;
	}
}
