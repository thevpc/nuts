package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.reserved.util.NUnsafeCallableWithDescription;
import net.thevpc.nuts.util.NUnsafeCallable;

import java.util.function.Supplier;

public abstract class NUnsafeCallableBase<T> implements NUnsafeCallable<T> {
    @Override
    public NUnsafeCallable<T> redescribe(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NUnsafeCallableWithDescription<>(this, description);
    }
}
