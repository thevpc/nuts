package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public abstract class NIteratorBase<T> implements NIterator<T> {

    public NIterator<T> redescribe(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NIteratorWithDescription<>(this, description);
    }

}
