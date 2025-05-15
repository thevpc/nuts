package net.thevpc.nuts.util;

/**
 * @author vpc
 */
public class NShortRef extends NObjectRef<Short> {

    public NShortRef(Short value) {
        super(value);
    }

    public NShortRef inc() {
        return inc((short)1);
    }

    public NShortRef inc(short value) {
        return add(value);
    }

    public NShortRef add(short value) {
        final Short o = get();
        if (o == null) {
            set(value);
        } else {
            set((short) (value + o));
        }
        return this;
    }

    public NShortRef mul(short value) {
        final Short o = get();
        if (o == null) {
            set(value);
        } else {
            set((short) (o * value));
        }
        return this;
    }

    public NShortRef div(short value) {
        final Short o = get();
        if (o == null) {
            set(value);
        } else {
            set((short) (o / value));
        }
        return this;
    }

    public NShortRef dec() {
        return add((short) -1);
    }

    public NShortRef dec(short value) {
        return add((short) -value);
    }

}
