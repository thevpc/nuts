package net.thevpc.nuts.util;

/**
 * @author vpc
 */
public class NByteRef extends NObjectRef<Byte> {
    /**
     * Creates a new instance.
     *
     * @return of result
     */
    public static NByteRef of(){
        return new NByteRef(null);
    }
    /**
     * Creates a new instance.
     *
     * @param value value
     * @return of result
     */
    public static NByteRef of(Byte value){
        return new NByteRef(value);
    }

    /**
     * N byte ref.
     *
     * @param value value
     * @return n byte ref result
     */
    public NByteRef(Byte value) {
        super(value);
    }

    /**
     * Inc.
     *
     * @return inc result
     */
    public NByteRef inc() {
        return inc((byte) 1);
    }

    /**
     * Inc.
     *
     * @param value value
     * @return inc result
     */
    public NByteRef inc(byte value) {
        return add(value);
    }

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    public NByteRef add(byte value) {
        final Byte o = get();
        if (o == null) {
            set(value);
        } else {
            set((byte) (value + o));
        }
        return this;
    }

    /**
     * Mul.
     *
     * @param value value
     * @return mul result
     */
    public NByteRef mul(byte value) {
        final Byte o = get();
        if (o == null) {
            set(value);
        } else {
            set((byte) (o * value));
        }
        return this;
    }

    /**
     * Div.
     *
     * @param value value
     * @return div result
     */
    public NByteRef div(byte value) {
        final Byte o = get();
        if (o == null) {
            set(value);
        } else {
            set((byte) (o / value));
        }
        return this;
    }

    /**
     * Dec.
     *
     * @return dec result
     */
    public NByteRef dec() {
        return add((byte) -1);
    }

    /**
     * Dec.
     *
     * @param value value
     * @return dec result
     */
    public NByteRef dec(byte value) {
        return add((byte) (-value));
    }

}
