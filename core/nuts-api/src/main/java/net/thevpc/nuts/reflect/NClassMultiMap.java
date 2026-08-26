package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;
import java.util.Set;

/**
 * NClassMultiMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NClassMultiMap<K, V> {
    /**
     * Creates a new instance of of.
     *
     * @param valueType value type
     * @return of result
     */
    static <V> NClassMultiMap<Object, V> of(Class<V> valueType) {
        /**
         * Creates a new instance of of.
         *
         * @param null null
         * @param valueType value type
         * @return of result
         */
        return of(null, valueType);
    }

    /**
     * Creates a new instance of of.
     *
     * @param key1Type key1 type
     * @param valueType value type
     * @return of result
     */
    static <K, V> NClassMultiMap<K, V> of(Class<K> key1Type, Class<V> valueType) {
        return NUtilsRPI.of().createClassMultiMap(key1Type, valueType);
    }

    /**
     * Adds add.
     *
     * @param classKey class key
     * @param value value
     */
    void add(Class<? extends K> classKey, V value);

    /**
     * Adds the specified all.
     *
     * @param other other
     */
    void addAll(NClassMultiMap<K, V> other);

    /**
     * Key set.
     *
     * @return key set result
     */
    Set<Class<? extends K>> keySet();

    /**
     * Finds the find matches.
     *
     * @param classKey class key
     * @return find matches result
     */
    List<V> findMatches(Class<? extends K> classKey);

    /**
     * Returns the get.
     *
     * @param classKey class key
     * @return get result
     */
    List<V> get(Class<? extends K> classKey);

    /**
     * Returns the exact.
     *
     * @param classKey class key
     * @return get exact result
     */
    List<V> getExact(Class<? extends K> classKey);

    /**
     * Clear.
     *
     * @param classKey class key
     * @return clear result
     */
    boolean clear(Class<? extends K> classKey);

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

    /**
     * Removes remove.
     *
     * @param classKey class key
     * @param value value
     * @return remove result
     */
    boolean remove(Class<? extends K> classKey, V value);

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Copy.
     *
     * @return copy result
     */
    NClassMultiMap<K, V> copy();
}
