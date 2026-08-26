package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

/**
 * NEvictingCharQueue interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEvictingCharQueue {
    /**
     * Creates a new instance of of.
     *
     * @param size size
     * @return of result
     */
    static NEvictingCharQueue of(int size) {
        return NUtilsRPI.of().createEvictingCharQueue(size);
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
    char get(int pos);

    /**
     * Adds add.
     *
     * @param t t
     */
    void add(char t);
}
