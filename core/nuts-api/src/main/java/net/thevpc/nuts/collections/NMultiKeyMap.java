package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.*;

/**
 * NMultiKeyMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NMultiKeyMap<K, V> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static <K, V> NMultiKeyMap<K, V> of() {
        return NUtilsRPI.of().createMultiKeyMap();
    }

    /**
     * Clear.
     */
    void clear();

    /**
     * All keys.
     *
     * @return all keys result
     */
    Set<K> allKeys();

    /**
     * Main keys.
     *
     * @return main keys result
     */
    Set<K> mainKeys();

    /**
     * Main size.
     *
     * @return main size result
     */
    int mainSize();

    /**
     * All size.
     *
     * @return all size result
     */
    int allSize();

    /**
     * Removes remove.
     *
     * @param k k
     * @return remove result
     */
    V remove(K k);

    /**
     * Put multi.
     *
     * @param k k
     * @param v v
     * @return put multi result
     */
    V putMulti(K[] k, V v);

    /**
     * Put.
     *
     * @param k k
     * @param v v
     * @return put result
     */
    V put(K k, V v);

    /**
     * Returns the get.
     *
     * @param k k
     * @return get result
     */
    V get(K k);

    /**
     * Merge keys.
     *
     * @param k1 k1
     * @param k2 k2
     */
    void mergeKeys(K k1, K k2);
}
