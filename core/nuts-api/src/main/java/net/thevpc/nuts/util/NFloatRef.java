package net.thevpc.nuts.util;

/**
 *
 * @author vpc
 */
public class NFloatRef extends NObjectRef<Float> {

    public NFloatRef(Float value) {
        super(value);
    }

    public NFloatRef inc() {
        return inc(1);
    }

    public NFloatRef inc(float value) {
        return add(value);
    }

    public NFloatRef add(float value) {
        final Float o = get();
        if (o == null) {
            set(value);
        } else {
            set(value + o);
        }
        return this;
    }

    public NFloatRef mul(float value) {
        final Float o = get();
        if (o == null) {
            set(value);
        } else {
            set(o * value);
        }
        return this;
    }

    public NFloatRef div(float value) {
        final Float o = get();
        if (o == null) {
            set(value);
        } else {
            set(o / value);
        }
        return this;
    }

    public NFloatRef dec() {
        return add(-1);
    }

    public NFloatRef dec(float value) {
        return add(-value);
    }

}
