package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NCappedMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * NConcurrentReadWriteLRUMap class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NConcurrentReadWriteLRUMap<K, V> extends LinkedHashMap<K, V> implements NCappedMap<K, V> {
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();
    private int maxSize;

    /**
     * N concurrent read write lru map.
     *
     * @param maxSize max size
     * @return n concurrent read write lru map result
     */
    public NConcurrentReadWriteLRUMap(int maxSize) {
      /**
       * Super.
       *
       * @param 1 1
       * @param 1.0f 1.0f
       * @param true true
       */
        super(maxSize + 1, 1.0f, true);
        this.maxSize = maxSize;
    }

    @Override
    public V get(Object key) {
        readLock.lock();
        try {
            return super.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the or default.
     *
     * @param key key
     * @param defaultValue default value
     * @return get or default result
     */
    public V getOrDefault(Object key, V defaultValue) {
        readLock.lock();
        try {
            return super.getOrDefault(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        writeLock.lock();
        try {
            return super.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return this.size() > maxSize;
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
