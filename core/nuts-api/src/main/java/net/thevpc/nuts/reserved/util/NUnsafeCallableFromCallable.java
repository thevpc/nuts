package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NUnsafeCallableBase;
import net.thevpc.nuts.util.NCallable;

public class NUnsafeCallableFromCallable<T> extends NUnsafeCallableBase<T> {
    private final NCallable<T> base;

    public NUnsafeCallableFromCallable(NCallable<T> base) {
        this.base = base;
    }

    @Override
    public NElement describe() {
        return base.describe();
    }

    @Override
    public T call() throws Exception {
        return base.call();
    }
}
