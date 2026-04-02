package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NListMultiValueMap<K, V> {

    static <K, V> NListMultiValueMap<K, V> of(){
        return NCollectionsRPI.of().createListMultiValueMap();
    }

    static <K, V> NListMultiValueMap<K, V> of(Map<K, List<V>> map){
        return NCollectionsRPI.of().createListMultiValueMap(map);
    }

    V getFirst(K a);

    boolean containsEntry(K a, V value);

    void add(K k, V v);

    boolean remove(K a, V value);

    int keyCount();

    int valueCount();

    List<V> get(K a);

    Set<Map.Entry<K, List<V>>> entrySet();

    Set<K> keySet();

    boolean containsKey(K key);
}
