package net.thevpc.nuts.util;

/**
 * @author vpc
 */
public class NByteRef extends NObjectRef<Byte> {

    public NByteRef(Byte value) {
        super(value);
    }

    public NByteRef inc() {
        return inc((byte) 1);
    }

    public NByteRef inc(byte value) {
        return add(value);
    }

    public NByteRef add(byte value) {
        final Byte o = get();
        if (o == null) {
            set(value);
        } else {
            set((byte) (value + o));
        }
        return this;
    }

    public NByteRef mul(byte value) {
        final Byte o = get();
        if (o == null) {
            set(value);
        } else {
            set((byte) (o * value));
        }
        return this;
    }

    public NByteRef div(byte value) {
        final Byte o = get();
        if (o == null) {
            set(value);
        } else {
            set((byte) (o / value));
        }
        return this;
    }

    public NByteRef dec() {
        return add((byte) -1);
    }

    public NByteRef dec(byte value) {
        return add((byte) (-value));
    }

}
