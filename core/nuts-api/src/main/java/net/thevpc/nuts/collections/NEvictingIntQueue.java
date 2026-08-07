package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

public interface NEvictingIntQueue {
    static NEvictingIntQueue of(int size) {
        return NUtilsRPI.of().createEvictingIntQueue(size);
    }
    void clear();

    int size();

    int get(int pos);

    void add(int t);
}
