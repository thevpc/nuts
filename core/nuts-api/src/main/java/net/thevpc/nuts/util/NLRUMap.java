package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.Map;

public interface NLRUMap<K, V> extends Map<K, V> {
    static <K, V> NLRUMap<K, V> of(int maxEntries) {
        return NUtilsRPI.of().createLruMap(maxEntries);
    }

    void resize(int maxEntries);
}
