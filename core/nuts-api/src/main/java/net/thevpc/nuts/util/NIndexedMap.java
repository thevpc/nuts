package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface NIndexedMap<K, V> {
    static <K, V> NIndexedMap<K, V> of(){
        return NCollectionsRPI.of().createIndexedMap();
    }

    V getValueAt(int i);

    void clear();

    int indexOfKey(K key);

    V get(K key);

    K getKeyAt(int i);

    Set<K> keySet();

    Set<Map.Entry<K, V>> entrySet();

    Collection<V> values();

    int size();

    V remove(K key);

    V put(K key, V value);
}
