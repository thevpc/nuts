package net.thevpc.nuts.runtime.standalone.util.common;

import java.util.*;
import net.thevpc.nuts.NutsUnexpectedException;

public class MultiKeyMap<K, V> {

    private Map<K, Base<K, V>> keyMap = new LinkedHashMap<>();

    public void clear() {
        keyMap.clear();
    }

    public Set<K> allKeys() {
        return new HashSet<>(keyMap.keySet());
    }

    public Set<K> mainKeys() {
        LinkedHashSet<K> k = new LinkedHashSet<>();
        for (Base<K, V> value : keyMap.values()) {
            k.add(value.main);
        }
        return k;
    }

    public int mainSize() {
        return mainKeys().size();
    }

    public int allSize() {
        return keyMap.size();
    }

    public V remove(K k) {
        Base<K, V> s = keyMap.get(k);
        V old = null;
        if (s != null) {
            s.all.remove(k);
            old = s.value;
            keyMap.remove(k);
        }
        return old;
    }

    public V putMulti(K[] k, V v) {
        V o = put(k[0], v);
        for (int i = 1; i < k.length; i++) {
            mergeKeys(k[0], k[1]);
        }
        return o;
    }

    public V put(K k, V v) {
        Base<K, V> s = keyMap.get(k);
        if (s == null) {
            s = new Base<>(k);
            keyMap.put(k, s);
        }
        V old = s.value;
        s.value = v;
        return old;
    }

    public V get(K k) {
        Base<K, V> m = keyMap.get(k);
        if (m != null) {
            return m.value;
        }
        return null;
    }

    public void mergeKeys(K k1, K k2) {
        Base<K, V> s1 = keyMap.get(k1);
        Base<K, V> s2 = keyMap.get(k2);
        if (s1 != null && s2 == null) {
            s1 = new Base<K, V>(k1);
            s1.add(k1);
            s1.add(k2);
            keyMap.put(s1.main, s1);
        } else if (s1 != null) {
            s1.add(k2);
        } else if (s2 != null) {
            s2.add(k1);
        } else {
            if (s1 != s2) {
                throw new NutsUnexpectedException(null);
            }
        }
    }

    private static class Base<K, V> {

        K main;
        V value;
        Set<K> all = new HashSet<>();

        public Base(K main) {
            this.main = main;
            all.add(main);
        }

        void add(K k) {
            all.add(k);
        }
    }
}
