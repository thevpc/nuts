package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NOptionalMap;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultNOptionalMap<K, V> implements NOptionalMap<K, V> {
    private Map<K, V> map;

    public DefaultNOptionalMap() {
        this.map = new HashMap<>();
    }

    public DefaultNOptionalMap(Map<K, V> map) {
        this.map = map == null ? new HashMap<>() : map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public NOptionalMap<K, V> putAll(Map<K, V> map) {
        this.map.putAll(map);
        return this;
    }

    @Override
    public NOptionalMap<K, V> putIfAbsent(K key, V value) {
        this.map.putIfAbsent(key, value);
        return this;
    }

    @Override
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

    @Override
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

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public NOptional<V> remove(K key) {
        boolean c = this.map.containsKey(key);
        V ov = map.remove(key);
        if (ov == null) {
            return c ? NOptional.ofNull() : NOptional.ofNamedEmpty(NMsg.ofC("%s", key));
        }
        return NOptional.of(ov);
    }

    @Override
    public Map<K, V> toMap() {
        return new LinkedHashMap<>(map);
    }
}
