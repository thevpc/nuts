package net.thevpc.nuts.util;

import java.util.*;

/**
 * Simple List builder
 *
 * @param <K,V>
 */
public class NMapBuilder<K, V> {
    private Map<K, V> base;
    private boolean ignoreNulls;

    public static <K, V> NMapBuilder<K, V> of() {
        return new NMapBuilder<K, V>(new HashMap<>());
    }

    public static <K, V> NMapBuilder<K, V> ofLinked() {
        return new NMapBuilder<K, V>(new LinkedHashMap<>());
    }

    public static <K, V> NMapBuilder<K, V> ofSorted() {
        return new NMapBuilder<K, V>(new TreeMap<K, V>());
    }

    public static <K, V> NMapBuilder<K, V> ofSorted(Comparator<? super K> comparator) {
        return new NMapBuilder<K, V>(new TreeMap<K, V>(comparator));
    }

    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    public NMapBuilder<K, V> setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    public NMapBuilder(Map<K, V> base) {
        this.base = base == null ? new HashMap<>() : base;
    }

    public NMapBuilder<K, V> put(K k, V v) {
        if (ignoreNulls) {
            if (k != null) {
                base.put(k, v);
            }
        } else {
            base.put(k, v);
        }
        return this;
    }

    public NMapBuilder<K, V> putAll(Map.Entry<K, V>... k) {
        if (ignoreNulls) {
            if (k != null) {
                for (Map.Entry<K, V> k1 : k) {
                    if (k1 != null && k1.getKey() != null) {
                        base.put(k1.getKey(), k1.getValue());
                    }
                }
            }
        } else {
            for (Map.Entry<K, V> k1 : k) {
                base.put(k1.getKey(), k1.getValue());
            }
        }
        return this;
    }

    public NMapBuilder<K, V> putAll(Map<K, V> map) {
        if (ignoreNulls) {
            if (map != null) {
                for (Map.Entry<K, V> k1 : map.entrySet()) {
                    if (k1.getKey() != null) {
                        base.put(k1.getKey(), k1.getValue());
                    }
                }
            }
        } else {
            base.putAll(map);
        }
        return this;
    }

    public int size() {
        return base.size();
    }

    public Map<K, V> build() {
        return base;
    }
}
