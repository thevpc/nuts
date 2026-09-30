package net.thevpc.nuts.util;

/**
 *
 * @author vpc
 */
public class NIntRef extends NObjectRef<Integer> {
    /**
     * Creates a new instance.
     *
     * @return of result
     */
    public static NIntRef of(){
        return new NIntRef(null);
    }
    /**
     * Creates a new instance.
     *
     * @param value value
     * @return of result
     */
    public static NIntRef of(Integer value){
        return new NIntRef(value);
    }

    /**
     * N int ref.
     *
     * @param value value
     * @return n int ref result
     */
    public NIntRef(Integer value) {
        super(value);
    }

    /**
     * Inc.
     *
     * @return inc result
     */
    public NIntRef inc() {
        return inc(1);
    }

    /**
     * Inc.
     *
     * @param value value
     * @return inc result
     */
    public NIntRef inc(int value) {
        return add(value);
    }

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    public NIntRef add(int value) {
        final Integer o = get();
        if (o == null) {
            set(value);
        } else {
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
    public NIntRef mul(int value) {
        final Integer o = get();
        if (o == null) {
            set(value);
        } else {
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
    public NIntRef div(int value) {
        final Integer o = get();
        if (o == null) {
            set(value);
        } else {
            set(o / value);
        }
        return this;
    }

    /**
     * Dec.
     *
     * @return dec result
     */
    public NIntRef dec() {
        return add(-1);
    }

    /**
     * Dec.
     *
     * @param value value
     * @return dec result
     */
    public NIntRef dec(int value) {
        return add(-value);
    }

}
