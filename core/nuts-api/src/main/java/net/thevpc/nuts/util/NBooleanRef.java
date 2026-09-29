package net.thevpc.nuts.util;

/**
 * @author vpc
 */
public class NBooleanRef extends NObjectRef<Boolean> {

    /**
     * Creates a new instance.
     *
     * @param value value
     * @return of result
     */
    public static NBooleanRef of(Boolean value) {
        return new NBooleanRef(value);
    }

    /**
     * Creates a new instance.
     *
     * @param value value
     * @return of result
     */
    public static NBooleanRef of(boolean value) {
        return new NBooleanRef(value);
    }

    /**
     * Creates a new instance of false.
     *
     * @return of false result
     */
    public static NBooleanRef ofFalse() {
        return of(false);
    }

    /**
     * Creates a new instance of true.
     *
     * @return of true result
     */
    public static NBooleanRef ofTrue() {
        return of(true);
    }

    /**
     * Creates a new instance of null.
     *
     * @return of null result
     */
    public static NBooleanRef ofNull() {
        return of();
    }

    /**
     * Creates a new instance.
     *
     * @return of result
     */
    public static NBooleanRef of() {
        return new NBooleanRef(null);
    }

    /**
     * N boolean ref.
     *
     * @param value value
     * @return n boolean ref result
     */
    public NBooleanRef(Boolean value) {
        super(value);
    }

    /**
     * Flip.
     *
     * @return flip result
     */
    public NBooleanRef flip() {
        Boolean v = get();
        if (v != null) {
            set(!v);
        }
        return this;
    }

    /**
     * Sets the set.
     *
     * @return set result
     */
    public NBooleanRef set() {
        set(true);
        return this;
    }

    /**
     * Or.
     *
     * @param b b
     * @return or result
     */
    public NBooleanRef or(boolean b) {
        Boolean v = get();
        set(v == null ? b : v || b);
        return this;
    }

    /**
     * And.
     *
     * @param b b
     * @return and result
     */
    public NBooleanRef and(boolean b) {
        Boolean v = get();
        set(v == null ? b : v && b);
        return this;
    }

    /**
     * Sets the true.
     *
     * @return set true result
     */
    public NBooleanRef setTrue() {
        set(true);
        return this;
    }

    /**
     * Sets the false.
     *
     * @return set false result
     */
    public NBooleanRef setFalse() {
        set(false);
        return this;
    }

    /**
     * Flip or true.
     *
     * @return flip or true result
     */
    public NBooleanRef flipOrTrue() {
        Boolean v = get();
        if (v != null) {
            set(!v);
        } else {
            set(true);
        }
        return this;
    }

    /**
     * Flip or false.
     *
     * @return flip or false result
     */
    public NBooleanRef flipOrFalse() {
        Boolean v = get();
        if (v != null) {
            set(!v);
        } else {
            set(false);
        }
        return this;
    }

}
