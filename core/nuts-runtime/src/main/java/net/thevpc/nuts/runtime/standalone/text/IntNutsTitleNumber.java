package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsTitleNumber;

/**
 */
public class IntNutsTitleNumber implements NutsTitleNumber {
    private int value;

    public IntNutsTitleNumber(int value) {
        this.value = value;
    }

    @Override
    public NutsTitleNumber next() {
        return new IntNutsTitleNumber(value + 1);
    }

    @Override
    public NutsTitleNumber first() {
        return new IntNutsTitleNumber(1);
    }

    @Override
    public NutsTitleNumber none() {
        return new IntNutsTitleNumber(0);
    }

    @Override
    public boolean isNone() {
        return value == 0;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
