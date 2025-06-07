package net.thevpc.nuts.util;

/**
 *
 * @author vpc
 */
public class NDoubleRef extends NObjectRef<Double> {

    public NDoubleRef(Double value) {
        super(value);
    }

    public NDoubleRef inc() {
        return inc(1);
    }

    public NDoubleRef inc(double value) {
        return add(value);
    }

    public NDoubleRef add(double value) {
        final Double o = get();
        if (o == null) {
            set(value);
        } else {
            set(value + o);
        }
        return this;
    }

    public NDoubleRef mul(double value) {
        final Double o = get();
        if (o == null) {
            set(value);
        } else {
            set(o * value);
        }
        return this;
    }

    public NDoubleRef div(double value) {
        final Double o = get();
        if (o == null) {
            set(value);
        } else {
            set(o / value);
        }
        return this;
    }

    public NDoubleRef dec() {
        return add(-1);
    }

    public NDoubleRef dec(double value) {
        return add(-value);
    }

}
