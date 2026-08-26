package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;

/**
 * NOptionalMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NOptionalMap<K, V> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static <K, V> NOptionalMap<K, V> of(){
        return  NUtilsRPI.of().createOptionalMap();
    }
    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Put all.
     *
     * @param map map
     * @return put all result
     */
    NOptionalMap<K, V> putAll(Map<K, V> map);

    /**
     * Put if absent.
     *
     * @param key key
     * @param value value
     * @return put if absent result
     */
    NOptionalMap<K, V> putIfAbsent(K key, V value);

    /**
     * Put.
     *
     * @param key key
     * @param value value
     * @return put result
     */
    NOptional<V> put(K key, V value);

    /**
     * Returns the get.
     *
     * @param key key
     * @return get result
     */
    NOptional<V> get(K key);

    /**
     * Clear.
     */
    void clear();

    /**
     * Removes remove.
     *
     * @param key key
     * @return remove result
     */
    NOptional<V> remove(K key);

    /**
     * Converts to map.
     *
     * @return to map result
     */
    Map<K, V> toMap();
}
