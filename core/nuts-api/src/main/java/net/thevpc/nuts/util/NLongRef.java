package net.thevpc.nuts.util;

/**
 *
 * @author vpc
 */
public class NLongRef extends NObjectRef<Long> {

    public NLongRef(Long value) {
        super(value);
    }

    public NLongRef inc() {
        return inc(1);
    }

    public NLongRef inc(long value) {
        return add(value);
    }

    public NLongRef add(long value) {
        final Long o = get();
        if (o == null) {
            set(value);
        } else {
            set(value + o);
        }
        return this;
    }

    public NLongRef mul(long value) {
        final Long o = get();
        if (o == null) {
            set(value);
        } else {
            set(o * value);
        }
        return this;
    }

    public NLongRef div(long value) {
        final Long o = get();
        if (o == null) {
            set(value);
        } else {
            set(o / value);
        }
        return this;
    }

    public NLongRef dec() {
        return add(-1);
    }

    public NLongRef dec(long value) {
        return add(-value);
    }

}
