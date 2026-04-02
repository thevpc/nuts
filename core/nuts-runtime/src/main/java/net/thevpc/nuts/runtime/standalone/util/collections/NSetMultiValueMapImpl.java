package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NSetMultiValueMap;

import java.util.*;

/**
 * @author taha.bensalah@gmail.com on 7/22/16.
 */
public class NSetMultiValueMapImpl<K, V> implements NSetMultiValueMap<K, V> {

    private Map<K, Set<V>> entries = new HashMap<K, Set<V>>();

    public NSetMultiValueMapImpl(Map<K, Set<V>> entries) {
        this.entries = createMap();
        if (entries != null) {
            this.entries.putAll(entries);
        }
    }

    public NSetMultiValueMapImpl() {
        this.entries = createMap();
    }

    @Override
    public V getFirst(K a) {
        Set<V> all = entries.get(a);
        if (all == null) {
            return null;
        }
        if (!all.isEmpty()) {
            Iterator<V> it = all.iterator();
            if (it.hasNext()) {
                return it.next();
            }
        }
        return null;
    }

    @Override
    public boolean containsEntry(K a, V value) {
        Set<V> all = entries.get(a);
        if (all != null) {
            return all.contains(value);
        }
        return false;
    }

    @Override
    public void add(K k, V v) {
        getOrCreate(k).add(v);
    }

    private Set<V> getOrCreate(K k) {
        Set<V> list = entries.get(k);
        if (list == null) {
            list = createSet();
            entries.put(k, list);
        }
        return list;
    }

    @Override
    public boolean remove(K a, V value) {
        Set<V> all = entries.get(a);
        if (all != null) {
            return all.remove(value);
        }
        return false;
    }

    @Override
    public int keyCount() {
        return entries.size();
    }

    @Override
    public int valueCount() {
        int count = 0;
        for (Map.Entry<K, Set<V>> entry : entries.entrySet()) {
            count += entry.getValue().size();
        }
        return count;
    }

    @Override
    public Set<V> get(K a) {
        Set<V> all = entries.get(a);
        if (all == null) {
            return Collections.EMPTY_SET;
        }
        if (all.size() > 0) {
            return Collections.unmodifiableSet(all);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public Set<Map.Entry<K, Set<V>>> entrySet() {
        return entries.entrySet();
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    @Override
    public boolean containsKey(K key) {
        return entries.containsKey(key);
    }

    protected Map<K, Set<V>> createMap() {
        return new HashMap<K, Set<V>>();
    }

    protected Set<V> createSet() {
        return new LinkedHashSet<>();
    }
}
