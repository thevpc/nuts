package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;

import java.util.Iterator;
import java.util.function.Supplier;

public abstract class NIteratorBase<T> implements NIterator<T> {
    @Override
    public NIterator<T> onClose(Runnable closeHandler) {
        if (closeHandler == null) {
            return this;
        }
        return new NIteratorWithDescription<>(this, null, closeHandler);
    }

    public NIterator<T> withDescription(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NIteratorWithDescription<>(this, description, null);
    }
    @Override
    public final boolean hasNext() {
        boolean b = hasNextImpl();
        if(!b){
            close();
        }
        return b;
    }

    protected abstract boolean hasNextImpl();

}
