package net.thevpc.nuts.runtime.standalone.util;

import java.util.*;

public class NMapWithAlias<K, V> {
    private final Map<K, V> base = new LinkedHashMap<>();
    private final Map<K, K> aliasToBase = new LinkedHashMap<>();
    private final Map<K, Set<K>> baseToAliases = new LinkedHashMap<>();

    public NMapWithAlias<K, V> alias(K key, K value) {
        aliasToBase.put(key, value);
        baseToAliases.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(value);
        return this;
    }

    public Set<K> keySet() {
        return new LinkedHashSet<>(base.keySet());
    }

    public Set<K> keySetWithAlias() {
        LinkedHashSet<K> u = new LinkedHashSet<>(base.keySet());
        u.addAll(aliasToBase.keySet());
        return u;
    }

    public V put(K key, V value) {
        V u = base.put(key, value);
        return u;
    }
    public Set<K> keySetWithAlias(K name) {
        Set<K> a = baseToAliases.get(name);
        if (a != null) {
            return Collections.unmodifiableSet(a);
        }
        K base = aliasToBase.get(name);
        if (base != null) {
            a = baseToAliases.get(base);
            if (a != null) {
                return Collections.unmodifiableSet(a);
            }
        }
        return Collections.singleton(name);
    }
    public V get(K key) {
        if (base.containsKey(key)) {
            return base.get(key);
        }
        K u = aliasToBase.get(key);
        if (u != null) {
            return base.get(u);
        }
        return null;
    }
}
