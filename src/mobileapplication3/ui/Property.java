package mobileapplication3.ui;

public abstract class Property {
	private String name;
	private boolean isImmutable;

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

	public abstract short getValue();
	public abstract void setValue(short value);
}
