package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;

public interface NOptionalMap<K, V> {
    static <K, V> NOptionalMap<K, V> of(){
        return  NUtilsRPI.of().createOptionalMap();
    }
    int size();

    NOptionalMap<K, V> putAll(Map<K, V> map);

    NOptionalMap<K, V> putIfAbsent(K key, V value);

    NOptional<V> put(K key, V value);

    NOptional<V> get(K key);

    void clear();

    NOptional<V> remove(K key);

    Map<K, V> toMap();
}
