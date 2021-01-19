package net.thevpc.nuts.runtime.bundles.datastr;

import java.util.*;

/**
 * @author taha.bensalah@gmail.com on 7/22/16.
 */
public class ListMap<K, V> {

    private Map<K, List<V>> map = new HashMap<K, List<V>>();

    public ListMap(Map<K, List<V>> map) {
        this.map = createMap();
        if (map != null) {
            this.map.putAll(map);
        }
    }

    public ListMap() {
        this.map = createMap();
    }

    public V getOne(K a) {
        List<V> all = map.get(a);
        if (all == null) {
            return null;
        }
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    public boolean contains(K a, V value) {
        List<V> all = map.get(a);
        if (all != null) {
            return all.contains(value);
        }
        return false;
    }

    public void add(K k, V v) {
        get(k).add(v);
    }

    private List<V> get(K k) {
        List<V> list = map.get(k);
        if (list == null) {
            list = createList();
            map.put(k, list);
        }
        return list;
    }

    public boolean remove(K a, V value) {
        List<V> all = map.get(a);
        if (all != null) {
            return all.remove(value);
        }
        return false;
    }

    public int keySize() {
        return map.size();
    }

    public int valueSize() {
        int count = 0;
        for (Map.Entry<K, List<V>> entry : map.entrySet()) {
            count += entry.getValue().size();
        }
        return count;
    }

    public List<V> getAll(K a) {
        List<V> all = map.get(a);
        if (all == null) {
            return Collections.EMPTY_LIST;
        }
        if (all.size() > 0) {
            return Collections.unmodifiableList(all);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public Set<Map.Entry<K, List<V>>> entrySet() {
        return map.entrySet();
    }

    public Set<K> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    protected Map<K, List<V>> createMap() {
        return new HashMap<K, List<V>>();
    }

    protected List<V> createList() {
        return new ArrayList<V>();
    }
}
