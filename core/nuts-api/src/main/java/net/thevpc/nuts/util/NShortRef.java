package net.thevpc.nuts.util;

/**
 * @author vpc
 */
public class NShortRef extends NObjectRef<Short> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NShortRef of(){
        return new NShortRef(null);
    }
    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    public static NShortRef of(Short value){
        return new NShortRef(value);
    }

    /**
     * N short ref.
     *
     * @param value value
     * @return n short ref result
     */
    public NShortRef(Short value) {
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
    public NShortRef inc() {
        /**
         * Inc.
         *
         * @param (short)1 (short)1
         * @return inc result
         */
        return inc((short)1);
    }

    /**
     * Inc.
     *
     * @param value value
     * @return inc result
     */
    public NShortRef inc(short value) {
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
    public NShortRef add(short value) {
        final Short o = get();
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
            set((short) (value + o));
        }
        return this;
    }

    /**
     * Mul.
     *
     * @param value value
     * @return mul result
     */
    public NShortRef mul(short value) {
        final Short o = get();
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
            set((short) (o * value));
        }
        return this;
    }

    /**
     * Div.
     *
     * @param value value
     * @return div result
     */
    public NShortRef div(short value) {
        final Short o = get();
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
            set((short) (o / value));
        }
        return this;
    }

    /**
     * Dec.
     *
     * @return dec result
     */
    public NShortRef dec() {
        /**
         * Adds add.
         *
         * @param -1 -1
         * @return add result
         */
        return add((short) -1);
    }

    /**
     * Dec.
     *
     * @param value value
     * @return dec result
     */
    public NShortRef dec(short value) {
        /**
         * Adds add.
         *
         * @param -value -value
         * @return add result
         */
        return add((short) -value);
    }

}
