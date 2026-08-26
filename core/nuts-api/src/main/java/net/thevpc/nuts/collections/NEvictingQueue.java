package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

/**
 * NEvictingQueue interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEvictingQueue<T> extends List<T> {
    /**
     * Creates a new instance of of.
     *
     * @param size size
     * @return of result
     */
    static <T> NEvictingQueue<T> of(int size) {
        return NUtilsRPI.of().createEvictingQueue(size);
    }
}
