package net.thevpc.nuts.util;

/**
 * NPair interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NPair<A extends T, B extends T, T> extends NTuple<T> {
    /**
     * First.
     *
     * @return first result
     */
    A first();

    /**
     * Second.
     *
     * @return second result
     */
    B second();

    /**
     * Sets the set.
     *
     * @param t t
     * @param index index
     * @return set result
     */
    NPair<A, B, T> set(T t, int index);

    /**
     * First.
     *
     * @param t t
     * @return first result
     */
    NPair<A, B, T> first(A t);

    /**
     * Second.
     *
     * @param t t
     * @return second result
     */
    NPair<A, B, T> second(B t);
}
