package net.thevpc.nuts.collections;

import java.util.*;

/**
 * Simple List builder
 *
 * @param <K,V>
 */
public class NMapBuilder<K, V> {
    private Map<K, V> base;
    private boolean ignoreNulls;

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static <K, V> NMapBuilder<K, V> of() {
        return new NMapBuilder<K, V>(new HashMap<>());
    }

    /**
     * Creates a new instance of of linked.
     *
     * @return of linked result
     */
    public static <K, V> NMapBuilder<K, V> ofLinked() {
        return new NMapBuilder<K, V>(new LinkedHashMap<>());
    }

    /**
     * Creates a new instance of of sorted.
     *
     * @return of sorted result
     */
    public static <K, V> NMapBuilder<K, V> ofSorted() {
        return new NMapBuilder<K, V>(new TreeMap<K, V>());
    }

    /**
     * Creates a new instance of of sorted.
     *
     * @param comparator comparator
     * @return of sorted result
     */
    public static <K, V> NMapBuilder<K, V> ofSorted(Comparator<? super K> comparator) {
        return new NMapBuilder<K, V>(new TreeMap<K, V>(comparator));
    }

    /**
     * Checks if is ignore nulls.
     *
     * @return is ignore nulls result
     */
    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    /**
     * Sets the ignore nulls.
     *
     * @param ignoreNulls ignore nulls
     * @return set ignore nulls result
     */
    public NMapBuilder<K, V> setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    /**
     * N map builder.
     *
     * @param base base
     * @return n map builder result
     */
    public NMapBuilder(Map<K, V> base) {
        this.base = base == null ? new HashMap<>() : base;
    }

    /**
     * Put.
     *
     * @param k k
     * @param v v
     * @return put result
     */
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

    /**
     * Put all.
     *
     * @param k k
     * @return put all result
     */
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

    /**
     * Put all.
     *
     * @param map map
     * @return put all result
     */
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

    /**
     * Size.
     *
     * @return size result
     */
    public int size() {
        return base.size();
    }

    /**
     * Build.
     *
     * @return build result
     */
    public Map<K, V> build() {
        return base;
    }
}
