package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NListMultiValueMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NListMultiValueMap<K, V> {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static <K, V> NListMultiValueMap<K, V> of(){
        return NUtilsRPI.of().createListMultiValueMap();
    }

    /**
     * Creates a new instance of of.
     *
     * @param map map
     * @return of result
     */
    static <K, V> NListMultiValueMap<K, V> of(Map<K, List<V>> map){
        return NUtilsRPI.of().createListMultiValueMap(map);
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
    List<V> get(K a);

    /**
     * Entry set.
     *
     * @return entry set result
     */
    Set<Map.Entry<K, List<V>>> entrySet();

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
