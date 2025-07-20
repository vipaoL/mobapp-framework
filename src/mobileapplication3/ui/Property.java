package mobileapplication3.ui;

public class Property {
	private String name;
	private boolean isImmutable;
	private int value;
	private int minValue = Short.MIN_VALUE, maxValue = Short.MAX_VALUE;

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

	// TODO: replace with normal integers
	public short getMaxValue() {
		return (short) maxValue;
	}

	public short getMinValue() {
		return (short) minValue;
	}

	public final boolean isCalculated() {
		return isImmutable;
	}

	public short getValue() {
		return (short) value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public Property setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	public Property setMinValue(int minValue) {
		this.minValue = minValue;
		return this;
	}
}
