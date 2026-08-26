package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

/**
 * NEvictingIntQueue interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEvictingIntQueue {
    /**
     * Creates a new instance of of.
     *
     * @param size size
     * @return of result
     */
    static NEvictingIntQueue of(int size) {
        return NUtilsRPI.of().createEvictingIntQueue(size);
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
    int get(int pos);

    /**
     * Adds add.
     *
     * @param t t
     */
    void add(int t);
}
