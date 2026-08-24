package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * NIndexedMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NIndexedMap<K, V> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static <K, V> NIndexedMap<K, V> of(){
        return NUtilsRPI.of().createIndexedMap();
    }

    /**
     * Returns the value at.
     *
     * @param i i
     * @return get value at result
     */
    V getValueAt(int i);

    /**
     * Clear.
     */
    void clear();

    /**
     * Index of key.
     *
     * @param key key
     * @return index of key result
     */
    int indexOfKey(K key);

    /**
     * Returns the get.
     *
     * @param key key
     * @return get result
     */
    V get(K key);

    /**
     * Returns the key at.
     *
     * @param i i
     * @return get key at result
     */
    K getKeyAt(int i);

    /**
     * Key set.
     *
     * @return key set result
     */
    Set<K> keySet();

    /**
     * Entry set.
     *
     * @return entry set result
     */
    Set<Map.Entry<K, V>> entrySet();

    /**
     * Values.
     *
     * @return values result
     */
    Collection<V> values();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Removes remove.
     *
     * @param key key
     * @return remove result
     */
    V remove(K key);

    /**
     * Put.
     *
     * @param key key
     * @param value value
     * @return put result
     */
    V put(K key, V value);
}
