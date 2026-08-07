package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

public interface NEvictingByteQueue {
    static NEvictingByteQueue of(int size) {
        return NUtilsRPI.of().createEvictingByteQueue(size);
    }
    void clear();

    int size();

    byte get(int pos);

    void add(byte t);
}
