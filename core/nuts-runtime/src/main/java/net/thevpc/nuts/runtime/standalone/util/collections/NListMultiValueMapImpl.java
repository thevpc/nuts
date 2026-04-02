package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NListMultiValueMap;

import java.util.*;

/**
 * @author taha.bensalah@gmail.com on 7/22/16.
 */
public class NListMultiValueMapImpl<K, V> implements NListMultiValueMap<K, V> {

    private Map<K, List<V>> entries = new HashMap<K, List<V>>();

    public NListMultiValueMapImpl(Map<K, List<V>> entries) {
        this.entries = createMap();
        if (entries != null) {
            this.entries.putAll(entries);
        }
    }

    public NListMultiValueMapImpl() {
        this.entries = createMap();
    }

    @Override
    public V getFirst(K a) {
        List<V> all = entries.get(a);
        if (all == null) {
            return null;
        }
        if (!all.isEmpty()) {
            return all.get(0);
        }
        return null;
    }

    @Override
    public boolean containsEntry(K a, V value) {
        List<V> all = entries.get(a);
        if (all != null) {
            return all.contains(value);
        }
        return false;
    }

    @Override
    public void add(K k, V v) {
        getOrCreate(k).add(v);
    }

    private List<V> getOrCreate(K k) {
        List<V> list = entries.get(k);
        if (list == null) {
            list = createList();
            entries.put(k, list);
        }
        return list;
    }

    @Override
    public boolean remove(K a, V value) {
        List<V> all = entries.get(a);
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
        for (Map.Entry<K, List<V>> entry : entries.entrySet()) {
            count += entry.getValue().size();
        }
        return count;
    }

    @Override
    public List<V> get(K a) {
        List<V> all = entries.get(a);
        if (all == null) {
            return Collections.EMPTY_LIST;
        }
        if (all.size() > 0) {
            return Collections.unmodifiableList(all);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Set<Map.Entry<K, List<V>>> entrySet() {
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

    protected Map<K, List<V>> createMap() {
        return new HashMap<K, List<V>>();
    }

    protected List<V> createList() {
        return new ArrayList<V>();
    }
}
