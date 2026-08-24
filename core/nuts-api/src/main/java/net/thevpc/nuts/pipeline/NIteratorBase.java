package net.thevpc.nuts.pipeline;

import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

/**
 * NIteratorBase class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NIteratorBase<T> implements NIterator<T> {
    @Override
    public NIterator<T> onClose(Runnable closeHandler) {
        if (closeHandler == null) {
            return this;
        }
        return NIterator.ofWithDescription(this, null, closeHandler);
    }

    /**
     * With description.
     *
     * @param description description
     * @return with description result
     */
    public NIterator<T> withDescription(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return NIterator.ofWithDescription(this, description, null);
    }
    @Override
    public final boolean hasNext() {
        boolean b = hasNextImpl();
        if(!b){
          /**
           * Close.
           */
            close();
        }
        return b;
    }

    /**
     * Checks if has next impl.
     *
     * @return has next impl result
     */
    protected abstract boolean hasNextImpl();

}
