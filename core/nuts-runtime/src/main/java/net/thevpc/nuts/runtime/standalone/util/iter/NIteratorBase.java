package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.util.NIterator;

public abstract class NIteratorBase<T> implements NIterator<T> {

    public NIterator<T> withDesc(NEDesc description) {
        if (description == null) {
            return this;
        }
        return new NIteratorWithDescription<>(this, description);
    }

}
