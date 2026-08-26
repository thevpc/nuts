package net.thevpc.nuts.collections;

/**
 * NCollectionDiffApplier interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NCollectionDiffApplier<T> {
    /**
     * Adds add.
     *
     * @param e e
     * @param context context
     */
    void add(T e, NCollectionDiff<T> context);

    /**
     * Removes remove.
     *
     * @param e e
     * @param context context
     */
    void remove(T e, NCollectionDiff<T> context);

    /**
     * Update.
     *
     * @param e e
     * @param old old
     * @param context context
     */
    void update(T e, T old, NCollectionDiff<T> context);
}
