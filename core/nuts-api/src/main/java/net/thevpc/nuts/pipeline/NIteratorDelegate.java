package net.thevpc.nuts.pipeline;

import net.thevpc.nuts.elem.NElement;

import java.util.List;
import java.util.function.Consumer;

/**
 * NIteratorDelegate class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NIteratorDelegate<T> extends NIteratorBase<T> {
    /**
     * Base iterator.
     *
     * @return base iterator result
     */
    public abstract NIterator<T> baseIterator();

    @Override
    public boolean hasNextImpl() {
        /**
         * Base iterator.
         *
         * @param ).hasNext( ).has next(
         * @return base iterator result
         */
        return baseIterator().hasNext();
    }

    @Override
    public T next() {
        /**
         * Base iterator.
         *
         * @param ).next( ).next(
         * @return base iterator result
         */
        return baseIterator().next();
    }

    @Override
    public NElement describe() {
        /**
         * Base iterator.
         *
         * @param ).describe( ).describe(
         * @return base iterator result
         */
        return baseIterator().describe();
    }

    @Override
    public void remove() {
      /**
       * Base iterator.
       *
       * @param ).remove( ).remove(
       */
        baseIterator().remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
      /**
       * Base iterator.
       *
       * @param ).forEachRemaining(action ).for each remaining(action
       */
        baseIterator().forEachRemaining(action);
    }

    @Override
    public List<T> toList() {
        /**
         * Base iterator.
         *
         * @param ).toList( ).to list(
         * @return base iterator result
         */
        return baseIterator().toList();
    }
}
