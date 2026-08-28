package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NCollectionDiffChange;
import net.thevpc.nuts.util.NDiffMode;

import java.util.function.Function;

/**
 * NCollectionDiffChange class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCollectionDiffChangeImpl<T> implements NCollectionDiffChange<T> {
    private final NDiffMode mode;
    private final T oldValue;
    private final int oldPos;
    private final T newValue;
    final int newPos;

    /**
     * N collection diff change.
     *
     * @param mode mode
     * @param oldValue old value
     * @param newValue new value
     * @return n collection diff change result
     */
    public NCollectionDiffChangeImpl(NDiffMode mode, NValueAndPos<T> oldValue, NValueAndPos<T> newValue) {
      /**
       * This.
       *
       * @param mode mode
       * @param oldValue.value old value.value
       * @param newValue.value new value.value
       * @param oldValue.pos old value.pos
       * @param newValue.pos new value.pos
       */
        this(mode, oldValue.value, newValue.value, oldValue.pos, newValue.pos);
    }

    /**
     * N collection diff change.
     *
     * @param mode mode
     * @param oldValue old value
     * @param newValue new value
     * @param oldPos old pos
     * @param newPos new pos
     * @return n collection diff change result
     */
    public NCollectionDiffChangeImpl(NDiffMode mode, T oldValue, T newValue, int oldPos, int newPos) {
        this.mode = mode;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.oldPos = oldPos;
        this.newPos = newPos;
    }

    /**
     * Map.
     *
     * @param f f
     * @return map result
     */
    @Override
    public <H> NCollectionDiffChange<H> map(Function<T, H> f) {
        return new NCollectionDiffChangeImpl<>(
                mode,
                oldValue == null ? null : f.apply(oldValue),
                newValue == null ? null : f.apply(newValue),
                oldPos,
                newPos
        );
    }

    /**
     * Mode.
     *
     * @return mode result
     */
    @Override
    public NDiffMode mode() {
        return mode;
    }

    /**
     * Old value.
     *
     * @return old value result
     */
    @Override
    public T oldValue() {
        return oldValue;
    }

    /**
     * New value.
     *
     * @return new value result
     */
    @Override
    public T newValue() {
        return newValue;
    }
}
