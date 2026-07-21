package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

public interface NEvictingQueue<T> extends List<T> {
    static <T> NEvictingQueue<T> of(int size) {
        return NUtilsRPI.of().createEvictingQueue(size);
    }
}
