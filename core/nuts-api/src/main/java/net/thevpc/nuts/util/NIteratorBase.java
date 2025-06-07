package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;

public abstract class NIteratorBase<T> implements NIterator<T> {

    public NIterator<T> withDesc(NEDesc description) {
        if (description == null) {
            return this;
        }
        return new NIteratorWithDescription<>(this, description);
    }

}
