package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.reserved.util.NUnsafeCallableWithDescription;
import net.thevpc.nuts.util.NUnsafeCallable;

public abstract class NUnsafeCallableBase<T> implements NUnsafeCallable<T> {
    @Override
    public NUnsafeCallable<T> withDesc(NEDesc description) {
        if (description == null) {
            return this;
        }
        return new NUnsafeCallableWithDescription<>(this, description);
    }
}
