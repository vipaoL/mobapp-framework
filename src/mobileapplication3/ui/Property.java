// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

public class Property {
    private String name;
    private boolean isActive = true;
    private int value;
    private int minValue = Short.MIN_VALUE, maxValue = Short.MAX_VALUE;

    public Property(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (isActive()) {
            this.value = value;
        }
    }

    public int getMinValue() {
        return minValue;
    }

    public Property setMinValue(int minValue) {
        this.minValue = minValue;
        return this;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public Property setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
