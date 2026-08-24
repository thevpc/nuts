package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

/**
 * NEvictingByteQueue interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEvictingByteQueue {
    /**
     * Creates a new instance of of.
     *
     * @param size size
     * @return of result
     */
    static NEvictingByteQueue of(int size) {
        return NUtilsRPI.of().createEvictingByteQueue(size);
    }
    /**
     * Clear.
     */
    void clear();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Returns the get.
     *
     * @param pos pos
     * @return get result
     */
    byte get(int pos);

    /**
     * Adds add.
     *
     * @param t t
     */
    void add(byte t);
}
