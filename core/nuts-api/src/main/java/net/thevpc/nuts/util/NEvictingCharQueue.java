package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

public interface NEvictingCharQueue {
    static NEvictingCharQueue of(int size) {
        return NCollectionsRPI.of().createEvictingCharQueue(size);
    }

    void clear();

    int size();

    char get(int pos);

    void add(char t);
}
