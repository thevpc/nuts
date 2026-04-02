package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.reflect.NClassMap;
import net.thevpc.nuts.reflect.NClassMultiMap;

import java.util.*;

/**
 * Created by vpc on 11/22/16.
 */
public class NClassMultiMapImpl<K, V> implements NClassMultiMap<K, V> {
    private Class<K> keyType;
    private final Class<V> valueType;
    private final NClassMap<K, List<V>> values;

    public NClassMultiMapImpl(Class<K> keyType, Class<V> valueType) {
        values = NClassMap.of(keyType, (Class) List.class);
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public NClassMultiMapImpl(Class keyType, Class<V> valueType, int initialCapacity) {
        values = NClassMap.of(keyType, (Class) List.class, initialCapacity);
        this.valueType = valueType;
    }


    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public void add(Class<? extends K> classKey, V value) {
        List<V> list = getOrCreate(classKey);
        list.add(value);
    }

    @Override
    public void addAll(NClassMultiMap<K, V> other) {
        if (other != null) {
            for (Class<? extends K> k : other.keySet()) {
                List<V> e = other.getExact(k);
                if (e != null) {
                    for (V v : e) {
                        add(k, v);
                    }
                }
            }
        }
    }

    private List<V> getOrCreate(Class<? extends K> classKey) {
        List<V> list = values.getExact(classKey);
        if (list == null) {
            list = new ArrayList<V>();
            values.put(classKey, list);
        }
        return list;
    }


    @Override
    public Set<Class<? extends K>> keySet() {
        return values.keySet();
    }

    @Override
    public List<V> findMatches(Class<? extends K> classKey) {
        List<V> all = new ArrayList<>();
        for (List<V> ts : values.findMatches(classKey)) {
            all.addAll(ts);
        }
        return all;
    }

    @Override
    public List<V> get(Class<? extends K> classKey) {
        List<V> all = new ArrayList<>();
        List<V> vs = values.get(classKey);
        if (vs != null) {
            all.addAll(vs);
        }
        return all;
    }

    @Override
    public List<V> getExact(Class<? extends K> classKey) {
        List<V> list = values.getExact(classKey);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean clear(Class<? extends K> classKey) {
        return values.remove(classKey) != null;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean clear() {
        if (!values.isEmpty()) {
            values.clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Class<? extends K> classKey, V value) {
        List<V> a = values.getExact(classKey);
        if (a != null) {
            if (a.remove(value)) {
                if (a.isEmpty()) {
                    values.remove(classKey);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NClassMultiMap<K, V> copy() {
        NClassMultiMapImpl<K, V> c = new NClassMultiMapImpl<>(keyType, valueType, values.size());
        c.addAll(this);
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NClassMultiMapImpl<?, ?> that = (NClassMultiMapImpl<?, ?>) o;
        return
                Objects.equals(keyType, that.keyType)
                        &&
                        Objects.equals(valueType, that.valueType)
                        &&
                        Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyType, valueType, values);
    }
}
