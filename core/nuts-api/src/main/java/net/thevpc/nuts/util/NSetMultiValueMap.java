package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NSetMultiValueMap<K, V> {

    static <K, V> NSetMultiValueMap<K, V> of(){
        return NCollectionsRPI.of().createSetMultiValueMap();
    }

    static <K, V> NSetMultiValueMap<K, V> of(Map<K, Set<V>> map){
        return NCollectionsRPI.of().createSetMultiValueMap(map);
    }

    V getFirst(K a);

    boolean containsEntry(K a, V value);

    void add(K k, V v);

    boolean remove(K a, V value);

    int keyCount();

    int valueCount();

    Set<V> get(K a);

    Set<Map.Entry<K, Set<V>>> entrySet();

    Set<K> keySet();

    boolean containsKey(K key);
}
