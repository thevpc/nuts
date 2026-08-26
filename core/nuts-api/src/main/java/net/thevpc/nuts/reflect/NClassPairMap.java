package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NTuple;

import java.util.List;
import java.util.Set;

/**
 * NClassPairMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NClassPairMap<A,B,V> {

    /**
     * Creates a new instance of of.
     *
     * @param clazz clazz
     * @return of result
     */
    static <A,B,V> NClassPairMap<A,B,V> of(Class<V> clazz) {
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
    static <A,B,V> NClassPairMap<A,B,V> of(Class<A> key1Type, Class<B> key2Type, Class<V> valueType, boolean symmetric) {
        return NUtilsRPI.of().createClassPairMap(key1Type, key2Type, valueType, symmetric);
    }


    /**
     * Key set.
     *
     * @return key set result
     */
    Set<NTuple<Class>> keySet();

    /**
     * Put.
     *
     * @param classKey1 class key1
     * @param classKey2 class key2
     * @param value value
     * @return put result
     */
    V put(Class<? extends A> classKey1, Class<? extends B> classKey2, V value);

    /**
     * Removes remove.
     *
     * @param classKey1 class key1
     * @param classKey2 class key2
     * @return remove result
     */
    V remove(Class<? extends A> classKey1, Class<? extends B> classKey2);

    /**
     * Returns the search path.
     *
     * @param classKey1 class key1
     * @param classKey2 class key2
     * @return get search path result
     */
    List<NTuple<Class>> getSearchPath(Class<? extends A> classKey1, Class<? extends B> classKey2);

    /**
     * Returns the exact.
     *
     * @param classKey1 class key1
     * @param classKey2 class key2
     * @return get exact result
     */
    V getExact(Class<? extends A> classKey1, Class<? extends B> classKey2);

    /**
     * Returns the get.
     *
     * @param classKey1 class key1
     * @param classKey2 class key2
     * @return get result
     */
    V get(Class<? extends A> classKey1, Class<? extends B> classKey2);

    /**
     * Finds the find matches.
     *
     * @param classKey1 class key1
     * @param classKey2 class key2
     * @return find matches result
     */
    List<V> findMatches(Class<? extends A> classKey1, Class<? extends B> classKey2);

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();
    /**
     * Clear.
     *
     * @return clear result
     */
    boolean clear();
}
