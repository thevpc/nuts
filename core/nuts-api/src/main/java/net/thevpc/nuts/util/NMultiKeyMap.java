package net.thevpc.nuts.util;

import java.util.*;

public interface NMultiKeyMap<K, V> {

    public void clear();

    public Set<K> allKeys();

    public Set<K> mainKeys() ;

    public int mainSize();

    public int allSize();

    public V remove(K k) ;

    public V putMulti(K[] k, V v) ;

    public V put(K k, V v) ;

    public V get(K k) ;

    public void mergeKeys(K k1, K k2) ;
}
