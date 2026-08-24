package net.thevpc.nuts.util;

/**
 * NTriplet interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTriplet<A extends T,B extends T,C extends T,T> extends NTuple<T> {
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
     * Third.
     *
     * @return third result
     */
    C third();

    /**
     * Sets the set.
     *
     * @param t t
     * @param index index
     * @return set result
     */
    NTriplet<A,B,C,T> set(T t, int index);

    /**
     * First.
     *
     * @param t t
     * @return first result
     */
    NTriplet<A,B,C,T> first(A t);

    /**
     * Second.
     *
     * @param t t
     * @return second result
     */
    NTriplet<A,B,C,T> second(B t);

    /**
     * Third.
     *
     * @param t t
     * @return third result
     */
    NTriplet<A,B,C,T> third(C t);

}
