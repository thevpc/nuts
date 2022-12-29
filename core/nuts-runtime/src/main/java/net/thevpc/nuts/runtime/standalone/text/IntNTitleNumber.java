package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NTitleNumber;

/**
 */
public class IntNTitleNumber implements NTitleNumber {
    private int value;

    public IntNTitleNumber(int value) {
        this.value = value;
    }

    @Override
    public NTitleNumber next() {
        return new IntNTitleNumber(value + 1);
    }

    @Override
    public NTitleNumber first() {
        return new IntNTitleNumber(1);
    }

    @Override
    public NTitleNumber none() {
        return new IntNTitleNumber(0);
    }

    @Override
    public boolean isNone() {
        return value == 0;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean isBullet() {
        return false;
    }
}
