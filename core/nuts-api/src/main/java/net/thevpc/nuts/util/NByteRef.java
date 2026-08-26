package net.thevpc.nuts.util;

/**
 * @author vpc
 */
public class NByteRef extends NObjectRef<Byte> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NByteRef of(){
        return new NByteRef(null);
    }
    /**
     * Creates a new instance of of.
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
      /**
       * Super.
       *
       * @param value value
       */
        super(value);
    }

    /**
     * Inc.
     *
     * @return inc result
     */
    public NByteRef inc() {
        /**
         * Inc.
         *
         * @param 1 1
         * @return inc result
         */
        return inc((byte) 1);
    }

    /**
     * Inc.
     *
     * @param value value
     * @return inc result
     */
    public NByteRef inc(byte value) {
        /**
         * Adds add.
         *
         * @param value value
         * @return add result
         */
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
          /**
           * Sets the set.
           *
           * @param value value
           */
            set(value);
        } else {
          /**
           * Sets the set.
           *
           * @param o) o)
           */
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
          /**
           * Sets the set.
           *
           * @param value value
           */
            set(value);
        } else {
          /**
           * Sets the set.
           *
           * @param value) value)
           */
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
          /**
           * Sets the set.
           *
           * @param value value
           */
            set(value);
        } else {
          /**
           * Sets the set.
           *
           * @param value) value)
           */
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
        /**
         * Adds add.
         *
         * @param -1 -1
         * @return add result
         */
        return add((byte) -1);
    }

    /**
     * Dec.
     *
     * @param value value
     * @return dec result
     */
    public NByteRef dec(byte value) {
        /**
         * Adds add.
         *
         * @param (-value) (-value)
         * @return add result
         */
        return add((byte) (-value));
    }

}
