package net.thevpc.nuts.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NOptionalMap<K, V> {
    private Map<K, V> map;

    public NOptionalMap() {
        this.map = new HashMap<>();
    }

    public NOptionalMap(Map<K, V> map) {
        this.map = map == null ? new HashMap<>() : map;
    }

    public int size() {
        return map.size();
    }

    public NOptionalMap<K, V> putAll(Map<K, V> map) {
        this.map.putAll(map);
        return this;
    }

    public NOptionalMap<K, V> putIfAbsent(K key, V value) {
        this.map.putIfAbsent(key, value);
        return this;
    }

    public NOptional<V> put(K key, V value) {
        boolean c = this.map.containsKey(key);
        V u = this.map.put(key, value);
        if (u == null) {
            if (this.map.containsKey(key)) {
                return NOptional.ofNull();
            }
            return NOptional.ofNamedEmpty(NMsg.ofC("%s", key));
        }
        return NOptional.of(u);
    }

    public NOptional<V> get(K key) {
        V u = this.map.get(key);
        if (u == null) {
            if (this.map.containsKey(key)) {
                return NOptional.ofNull();
            }
            return NOptional.ofNamedEmpty(NMsg.ofC("%s", key));
        }
        return NOptional.of(u);
    }

    public void clear() {
        map.clear();
    }

    public NOptional<V> remove(K key) {
        boolean c = this.map.containsKey(key);
        V ov = map.remove(key);
        if (ov == null) {
            return c ? NOptional.ofNull() : NOptional.ofNamedEmpty(NMsg.ofC("%s", key));
        }
        return NOptional.of(ov);
    }

    public Map<K, V> toMap() {
        return new LinkedHashMap<>(map);
    }
}
