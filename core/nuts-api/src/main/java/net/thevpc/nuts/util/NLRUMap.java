package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.Map;

public interface NLRUMap<K, V> extends Map<K, V> {
    static <K, V> NLRUMap<K, V> of(int maxEntries) {
        return NCollectionsRPI.of().createLruMap(maxEntries);
    }

    void resize(int maxEntries);
}
