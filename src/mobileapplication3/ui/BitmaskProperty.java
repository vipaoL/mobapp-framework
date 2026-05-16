// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.ui;

public class BitmaskProperty extends Property {
    private int bitsCount;

    public BitmaskProperty(String name, int bitsCount) {
        super(name);
        this.bitsCount = bitsCount;
    }

    public int getBitsCount() {
        return bitsCount;
    }

    public void setBitsCount(int bitsCount) {
        this.bitsCount = bitsCount;
    }
}
