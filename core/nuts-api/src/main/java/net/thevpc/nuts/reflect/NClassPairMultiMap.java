package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NTuple;

import java.util.List;
import java.util.Set;

/**
 * NClassPairMultiMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NClassPairMultiMap<A, B, V> {
    /**
     * Creates a new instance of of.
     *
     * @param clazz clazz
     * @return of result
     */
    static <A, B, V> NClassPairMultiMap<A, B, V> of(Class<V> clazz) {
        /**
         * Creates a new instance of of.
         *
         * @param null null
         * @param null null
         * @param clazz clazz
         * @param false false
         * @return of result
         */
        return of(null, null, clazz, false);
    }

    /**
     * Creates a new instance of of.
     *
     * @param key1Type key1 type
     * @param key2Type key2 type
     * @param valueType value type
     * @param symmetric symmetric
     * @return of result
     */
    static <A, B, V> NClassPairMultiMap<A, B, V> of(Class<A> key1Type, Class<B> key2Type, Class<V> valueType, boolean symmetric) {
        return NUtilsRPI.of().createClassPairMultiMap(key1Type, key2Type, valueType, symmetric);
    }

    /**
     * Key set.
     *
     * @return key set result
     */
    Set<NTuple<Class>> keySet();

    /**
     * Adds add.
     *
     * @param a a
     * @param b b
     * @param value value
     */
    void add(Class<? extends A> a, Class<? extends B> b, V value);

    /**
     * Removes remove.
     *
     * @param a a
     * @param b b
     * @param value value
     */
    void remove(Class<? extends A> a, Class<? extends B> b, V value);

    /**
     * Returns the "Best Match" bucket.
     * Finds the closest list in the hierarchy and returns its contents.
     */
    List<V> get(Class<? extends A> a, Class<? extends B> b);

    /**
     * Returns the "Cumulative Match" list.
     * Finds ALL buckets in the hierarchy and flattens them into one list.
     */
    List<V> findMatches(Class<? extends A> a, Class<? extends B> b);

    /**
     * Returns only the bucket for the exact class pair.
     */
    List<V> getExact(Class<? extends A> a, Class<? extends B> b);

    /**
     * Clear.
     *
     * @param a a
     * @param b b
     * @return clear result
     */
    boolean clear(Class<? extends A> a, Class<? extends B> b);

    /**
     * Clear.
     *
     * @return clear result
     */
    boolean clear();

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

}
