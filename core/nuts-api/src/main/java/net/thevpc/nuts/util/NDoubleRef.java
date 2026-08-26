package net.thevpc.nuts.util;

/**
 *
 * @author vpc
 */
public class NDoubleRef extends NObjectRef<Double> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NDoubleRef of(){
        return new NDoubleRef(null);
    }
    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    public static NDoubleRef of(Double value){
        return new NDoubleRef(value);
    }

    /**
     * N double ref.
     *
     * @param value value
     * @return n double ref result
     */
    public NDoubleRef(Double value) {
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
    public NDoubleRef inc() {
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
    public NDoubleRef inc(double value) {
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
    public NDoubleRef add(double value) {
        final Double o = get();
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
    public NDoubleRef mul(double value) {
        final Double o = get();
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
    public NDoubleRef div(double value) {
        final Double o = get();
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
    public NDoubleRef dec() {
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
    public NDoubleRef dec(double value) {
        /**
         * Adds add.
         *
         * @param -value -value
         * @return add result
         */
        return add(-value);
    }

}
