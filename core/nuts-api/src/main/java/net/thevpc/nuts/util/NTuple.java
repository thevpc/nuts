package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.List;

/**
 * NTuple interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTuple<T> extends Iterable<T> {
    /**
     * Creates a new instance of of.
     *
     * @param a a
     * @param b b
     * @return of result
     */
    static <A extends T,B extends T,T> NPair<A,B,T> of(A a, B b) {
        return new NPairImpl<>(a, b);
    }

    /**
     * Creates a new instance of of.
     *
     * @param a a
     * @param b b
     * @param c c
     * @return of result
     */
    static <A extends T,B extends T,C extends T,T> NTuple<T> of(A a, B b, C c) {
        return new NTripletImpl<>(a,b,c);
    }

    /**
     * Creates a new instance of of.
     *
     * @param a a
     * @return of result
     */
    static <T> NTuple<T> of(T... a) {
        switch (a.length) {
            case 2:{
                /**
                 * Creates a new instance of of.
                 *
                 * @param a[0] a[0]
                 * @param a[1] a[1]
                 * @return of result
                 */
                return of(a[0],a[1]);
            }
            case 3:{
                /**
                 * Creates a new instance of of.
                 *
                 * @param a[0] a[0]
                 * @param a[1] a[1]
                 * @param a[3] a[3]
                 * @return of result
                 */
                return of(a[0],a[1],a[3]);
            }
        }
        return new NTupleImpl<>(Arrays.copyOf(a,a.length));
    }

    /**
     * Returns the get.
     *
     * @param index index
     * @return get result
     */
    T get(int index);

    /**
     * Sets the set.
     *
     * @param newValue new value
     * @param index index
     * @return set result
     */
    NTuple<T> set(T newValue, int index);

    /**
     * Converts to list.
     *
     * @return to list result
     */
    List<T> toList();

    /**
     * Converts to array.
     *
     * @return to array result
     */
    T[] toArray();

    /**
     * Size.
     *
     * @return size result
     */
    int size();
}
