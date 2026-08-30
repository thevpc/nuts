package net.thevpc.nuts.collections;

import net.thevpc.nuts.util.NRenamed;
import net.thevpc.nuts.util.NSince;

import java.util.Map;

/**
 * A {@link Map} that is limited to a maximum number of entries.
 * <p>
 * When the map reaches its capacity, further insertions cause entries to be
 * evicted according to the concrete implementation’s policy
 * (for example LRU, LFU, FIFO, etc.).
 * <p>
 * The capacity can be changed at runtime via {@link #resize(int)}.
 * Reducing the capacity may trigger immediate eviction of excess entries.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @see java.util.Map
 */
@NSince("0.8.9")
@NRenamed(value = "NLRUMap", since = "1.0.0")
public interface NCappedMap<K, V> extends Map<K, V> {
    /**
     * Sets the maximum number of entries this map may hold.
     * If the new limit is smaller than the current size, excess entries
     * are evicted according to the map’s eviction policy.
     *
     * @param maxEntries the new maximum number of entries (must be ≥ 0)
     * @throws IllegalArgumentException if {@code maxEntries < 0}
     */
    void resize(int maxEntries);

    /**
     * Returns the current maximum number of entries
     * @return the current maximum number of entries
     */
    int maxEntries();
}
