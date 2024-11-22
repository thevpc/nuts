package net.thevpc.nuts.lib.common.collections;

public interface NCollectionDiffApplier<T> {
    void add(T e, NCollectionDiff<T> context);

    void remove(T e, NCollectionDiff<T> context);

    void update(T e, T old, NCollectionDiff<T> context);
}
