package net.thevpc.nuts.util;

import java.util.*;
import java.util.stream.Collectors;

public class NCaseInsensitiveStringMap<T> extends AbstractMap<String, T> implements Map<String, T> {
    private final Map<String, Map.Entry<String, T>> map = new HashMap<>();

    public NCaseInsensitiveStringMap() {

    }
    public NCaseInsensitiveStringMap(Map<String, T> other) {
        if(other != null) {
            putAll(other);
        }
    }

    @Override
    public Set<Entry<String, T>> entrySet() {
        return map.entrySet().stream().map(x -> new SimpleImmutableEntry<String, T>(x.getValue().getKey(), x.getValue().getValue())).collect(Collectors.toSet());
    }

    @Override
    public T get(Object key) {
        Entry<String, T> u = map.get(normalizeKey(key));
        return u == null ? null : u.getValue();
    }

    private static String normalizeKey(Object key) {
        return key == null ? null : key.toString().toLowerCase();
    }

    private static String normalizeKey(String key) {
        return key == null ? null : key.toLowerCase();
    }

    public T put(String key, T value) {
        Entry<String, T> old = map.put(normalizeKey(key), new SimpleImmutableEntry<>(key, value));
        return old == null ? null : old.getValue();
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(normalizeKey(key), value);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(normalizeKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }
}
