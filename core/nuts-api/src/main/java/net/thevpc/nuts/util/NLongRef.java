package net.thevpc.nuts.util;

/**
 *
 * @author vpc
 */
public class NLongRef extends NObjectRef<Long> {
    /**
     * Creates a new instance.
     *
     * @return of result
     */
    public static NLongRef of(){
        return new NLongRef(null);
    }
    /**
     * Creates a new instance.
     *
     * @param value value
     * @return of result
     */
    public static NLongRef of(Long value){
        return new NLongRef(value);
    }

    /**
     * N long ref.
     *
     * @param value value
     * @return n long ref result
     */
    public NLongRef(Long value) {
        super(value);
    }

    /**
     * Inc.
     *
     * @return inc result
     */
    public NLongRef inc() {
        return inc(1);
    }

    /**
     * Inc.
     *
     * @param value value
     * @return inc result
     */
    public NLongRef inc(long value) {
        return add(value);
    }

    /**
     * Adds add.
     *
     * @param value value
     * @return add result
     */
    public NLongRef add(long value) {
        final Long o = get();
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
    public NLongRef mul(long value) {
        final Long o = get();
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
    public NLongRef div(long value) {
        final Long o = get();
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
    public NLongRef dec() {
        return add(-1);
    }

    /**
     * Dec.
     *
     * @param value value
     * @return dec result
     */
    public NLongRef dec(long value) {
        return add(-value);
    }

}
