package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NCappedMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by vpc on 6/1/14.
 */
public class NLRUMapImpl<K, V> extends LinkedHashMap<K, V> implements NCappedMap<K, V> {

    private int maxSize;

    public NLRUMapImpl(final int maxSize) {
        super(maxSize + 1, 1.0f, true);
        this.maxSize = maxSize;
    }

    /**
     * Returns {@code true} if this <code>LruCache</code> has more entries than
     * the maximum specified when it was created.
     * <br>
     * <br>
     * This method <em>does not</em> modify the underlying <code>Map</code>; it
     * relies on the implementation of <code>LinkedHashMap</code> to do that,
     * but that behavior is documented in the JavaDoc for
     * <code>LinkedHashMap</code>.
     * <br>
     *
     * @param eldest the <code>Entry</code> in question; this implementation
     *               doesn't care what It's, since the implementation is only dependent on
     *               the size of the cache
     * @return {@code true} if the oldest
     * @see java.util.LinkedHashMap#removeEldestEntry(Map.Entry)
     */
    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return super.size() > maxSize;
    }

    @Override
    public void resize(int maxEntries) {
        //LRUMap<A, B> n = new LRUMap<A, B>(maxEntries);
        //n.putAll(this);
        int old = this.maxSize;
        this.maxSize = maxEntries;
        if (old > maxEntries) {
            int size = size();
            for (Iterator<Map.Entry<K, V>> iterator = this.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<K, V> abEntry = iterator.next();
                iterator.remove();
                size--;
                if (size <= maxEntries) {
                    break;
                }
            }
        }
    }

    @Override
    public int maxEntries() {
        return maxSize;
    }
}
