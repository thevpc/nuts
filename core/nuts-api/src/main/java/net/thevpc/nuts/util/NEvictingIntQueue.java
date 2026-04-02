package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

public interface NEvictingIntQueue {
    static NEvictingIntQueue of(int size) {
        return NCollectionsRPI.of().createEvictingIntQueue(size);
    }
    void clear();

    int size();

    int get(int pos);

    void add(int t);
}
