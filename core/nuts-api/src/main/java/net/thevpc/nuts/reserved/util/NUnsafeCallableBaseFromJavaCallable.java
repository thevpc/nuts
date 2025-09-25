package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NUnsafeCallableBase;

import java.util.concurrent.Callable;

public class NUnsafeCallableBaseFromJavaCallable<T> extends NUnsafeCallableBase<T> {
    private final Callable<T> base;

    public NUnsafeCallableBaseFromJavaCallable(Callable<T> base) {
        this.base = base;
    }

    @Override
    public NElement describe() {
        return NElementDescribables.ofDesc("callable").get();
    }

    @Override
    public T call() throws Exception {
        return base.call();
    }
}
