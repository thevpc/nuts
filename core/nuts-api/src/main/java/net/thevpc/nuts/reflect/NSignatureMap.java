package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NOptional;

import java.util.Map;

/**
 * NSignatureMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NSignatureMap<S extends NSignature<T, ?>, T, V> {
    /**
     * Put multi.
     *
     * @param sig sig
     * @param value value
     * @param sigs sigs
     */
    void putMulti(S sig, V value, S... sigs);

    /**
     * Put.
     *
     * @param sig sig
     * @param value value
     */
    void put(S sig, V value);

    /**
     * Returns the get.
     *
     * @param sig sig
     * @return get result
     */
    NOptional<V> get(S sig);

    /**
     * Removes remove.
     *
     * @param sig sig
     */
    void remove(S sig);

    /**
     * Converts to map.
     *
     * @return to map result
     */
    Map<S, V> toMap();

    /**
     * Size.
     *
     * @return size result
     */
    int size();
}
