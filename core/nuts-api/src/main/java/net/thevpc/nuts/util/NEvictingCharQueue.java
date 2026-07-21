package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

public interface NEvictingCharQueue {
    static NEvictingCharQueue of(int size) {
        return NUtilsRPI.of().createEvictingCharQueue(size);
    }

    void clear();

    int size();

    char get(int pos);

    void add(char t);
}
