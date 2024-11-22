package net.thevpc.nuts.util;

import java.util.function.Function;

public class NCollectionDiffChange<T> {
    private final NDiffMode mode;
    private final T oldValue;
    private final int oldPos;
    private final T newValue;
    final int newPos;

    public NCollectionDiffChange(NDiffMode mode, NValueAndPos<T> oldValue, NValueAndPos<T> newValue) {
        this(mode, oldValue.value, newValue.value, oldValue.pos, newValue.pos);
    }

    public NCollectionDiffChange(NDiffMode mode, T oldValue, T newValue, int oldPos, int newPos) {
        this.mode = mode;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.oldPos = oldPos;
        this.newPos = newPos;
    }

    public <H> NCollectionDiffChange<H> map(Function<T, H> f) {
        return new NCollectionDiffChange<>(
                mode,
                oldValue == null ? null : f.apply(oldValue),
                newValue == null ? null : f.apply(newValue),
                oldPos,
                newPos
        );
    }

    public NDiffMode getMode() {
        return mode;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return newValue;
    }
}
