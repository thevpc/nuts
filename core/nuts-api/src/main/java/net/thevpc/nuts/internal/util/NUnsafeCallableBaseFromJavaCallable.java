package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NUnsafeCallableBase;

import java.util.concurrent.Callable;

/**
 * NUnsafeCallableBaseFromJavaCallable class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NUnsafeCallableBaseFromJavaCallable<T> extends NUnsafeCallableBase<T> {
    private final Callable<T> base;

    /**
     * N unsafe callable base from java callable.
     *
     * @param base base
     * @return n unsafe callable base from java callable result
     */
    public NUnsafeCallableBaseFromJavaCallable(Callable<T> base) {
        this.base = base;
    }

    @Override
    public NElement describe() {
        return NDescribables.ofDesc("callable").get();
    }

    @Override
    public T call() throws Exception {
        return base.call();
    }
}
