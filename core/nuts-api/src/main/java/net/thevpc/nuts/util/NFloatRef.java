package net.thevpc.nuts.util;

/**
 *
 * @author vpc
 */
public class NFloatRef extends NObjectRef<Float> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NFloatRef of(){
        return new NFloatRef(null);
    }
    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    public static NFloatRef of(Float value){
        return new NFloatRef(value);
    }

    /**
     * N float ref.
     *
     * @param value value
     * @return n float ref result
     */
    public NFloatRef(Float value) {
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
    public NFloatRef inc() {
        /**
         * Inc.
         *
         * @param 1 1
         * @return inc result
         */
        return inc(1);
    }

    /**
     * Inc.
     *
     * @param value value
     * @return inc result
     */
    public NFloatRef inc(float value) {
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
    public NFloatRef add(float value) {
        final Float o = get();
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
           * @param o o
           */
            set(value + o);
        }
        return this;
    }

    /**
     * Mul.
     *
     * @param value value
     * @return mul result
     */
    public NFloatRef mul(float value) {
        final Float o = get();
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
           * @param value value
           */
            set(o * value);
        }
        return this;
    }

    /**
     * Div.
     *
     * @param value value
     * @return div result
     */
    public NFloatRef div(float value) {
        final Float o = get();
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
           * @param value value
           */
            set(o / value);
        }
        return this;
    }

    /**
     * Dec.
     *
     * @return dec result
     */
    public NFloatRef dec() {
        /**
         * Adds add.
         *
         * @param -1 -1
         * @return add result
         */
        return add(-1);
    }

    /**
     * Dec.
     *
     * @param value value
     * @return dec result
     */
    public NFloatRef dec(float value) {
        /**
         * Adds add.
         *
         * @param -value -value
         * @return add result
         */
        return add(-value);
    }

}
