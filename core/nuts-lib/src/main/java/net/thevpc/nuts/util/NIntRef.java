package net.thevpc.nuts.util;

/**
 *
 * @author vpc
 */
public class NIntRef extends NObjectRef<Integer> {

    public NIntRef(Integer value) {
        super(value);
    }

    public NIntRef inc() {
        return inc(1);
    }

    public NIntRef inc(int value) {
        return add(value);
    }

    public NIntRef add(int value) {
        final Integer o = get();
        if (o == null) {
            set(value);
        } else {
            set(value + o);
        }
        return this;
    }

    public NIntRef mul(int value) {
        final Integer o = get();
        if (o == null) {
            set(value);
        } else {
            set(o * value);
        }
        return this;
    }

    public NIntRef div(int value) {
        final Integer o = get();
        if (o == null) {
            set(value);
        } else {
            set(o / value);
        }
        return this;
    }

    public NIntRef dec() {
        return add(-1);
    }

    public NIntRef dec(int value) {
        return add(-value);
    }

}
