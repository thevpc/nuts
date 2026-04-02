package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.List;
import java.util.Set;

public interface NClassMultiMap<K, V> {
    static <V> NClassMultiMap<Object, V> of(Class<V> valueType) {
        return of(null, valueType);
    }

    static <K, V> NClassMultiMap<K, V> of(Class<K> key1Type, Class<V> valueType) {
        return NCollectionsRPI.of().createClassMultiMap(key1Type, valueType);
    }

    void add(Class<? extends K> classKey, V value);

    void addAll(NClassMultiMap<K, V> other);

    Set<Class<? extends K>> keySet();

    List<V> findMatches(Class<? extends K> classKey);

    List<V> get(Class<? extends K> classKey);

    List<V> getExact(Class<? extends K> classKey);

    boolean clear(Class<? extends K> classKey);

    boolean isEmpty();

    boolean clear();

    boolean remove(Class<? extends K> classKey, V value);

    int size();

    NClassMultiMap<K, V> copy();
}
