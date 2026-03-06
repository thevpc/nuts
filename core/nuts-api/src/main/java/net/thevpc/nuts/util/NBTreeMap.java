package net.thevpc.nuts.util;

import java.util.*;

public interface NBTreeMap<K extends Comparable<K>,V> {
    boolean put(K key, V value) ;

    int size();

    Set<K> keySet() ;

    Collection<V> values() ;

    Set<Map.Entry<K,V>> entrySet() ;

    boolean isEmpty() ;

    boolean contains(K key);

    public V get(K key) ;

    public V remove(K key, V value) ;
}
