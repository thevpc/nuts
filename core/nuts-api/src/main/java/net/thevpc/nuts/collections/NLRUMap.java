package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.Map;

/**
 * NLRUMap interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NLRUMap<K, V> extends Map<K, V> {
    /**
     * Creates a new instance of of.
     *
     * @param maxEntries max entries
     * @return of result
     */
    static <K, V> NLRUMap<K, V> of(int maxEntries) {
        return NUtilsRPI.of().createLruMap(maxEntries);
    }

    /**
     * Resize.
     *
     * @param maxEntries max entries
     */
    void resize(int maxEntries);
}
