package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NUnsafeCallableBase;
import net.thevpc.nuts.concurrent.NCallable;

/**
 * NUnsafeCallableFromCallable class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NUnsafeCallableFromCallable<T> extends NUnsafeCallableBase<T> {
    private final NCallable<T> base;

    /**
     * N unsafe callable from callable.
     *
     * @param base base
     * @return n unsafe callable from callable result
     */
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
