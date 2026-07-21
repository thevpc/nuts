package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.*;

public interface NMultiKeyMap<K, V> {
    static <K, V> NMultiKeyMap<K, V> of() {
        return NUtilsRPI.of().multiKeyMap();
    }

    void clear();

    Set<K> allKeys();

    Set<K> mainKeys();

    int mainSize();

    int allSize();

    V remove(K k);

    V putMulti(K[] k, V v);

    V put(K k, V v);

    V get(K k);

    void mergeKeys(K k1, K k2);
}
