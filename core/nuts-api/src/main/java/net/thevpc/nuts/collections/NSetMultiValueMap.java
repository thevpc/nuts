package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.Map;
import java.util.Set;

/**
 * NSetMultiValueMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NSetMultiValueMap<K, V> {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static <K, V> NSetMultiValueMap<K, V> of(){
        return NUtilsRPI.of().createSetMultiValueMap();
    }

    /**
     * Creates a new instance of of.
     *
     * @param map map
     * @return of result
     */
    static <K, V> NSetMultiValueMap<K, V> of(Map<K, Set<V>> map){
        return NUtilsRPI.of().createSetMultiValueMap(map);
    }

    /**
     * Returns the first.
     *
     * @param a a
     * @return get first result
     */
    V getFirst(K a);

    /**
     * Contains entry.
     *
     * @param a a
     * @param value value
     * @return contains entry result
     */
    boolean containsEntry(K a, V value);

    /**
     * Adds add.
     *
     * @param k k
     * @param v v
     */
    void add(K k, V v);

    /**
     * Removes remove.
     *
     * @param a a
     * @param value value
     * @return remove result
     */
    boolean remove(K a, V value);

    /**
     * Key count.
     *
     * @return key count result
     */
    int keyCount();

    /**
     * Value count.
     *
     * @return value count result
     */
    int valueCount();

    /**
     * Returns the get.
     *
     * @param a a
     * @return get result
     */
    Set<V> get(K a);

    /**
     * Entry set.
     *
     * @return entry set result
     */
    Set<Map.Entry<K, Set<V>>> entrySet();

    /**
     * Key set.
     *
     * @return key set result
     */
    Set<K> keySet();

    /**
     * Contains key.
     *
     * @param key key
     * @return contains key result
     */
    boolean containsKey(K key);
}
