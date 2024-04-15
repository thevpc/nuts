package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NUnsafeCallableBase;

import java.util.concurrent.Callable;

public class NUnsafeCallableBaseFromJavaCallable<T> extends NUnsafeCallableBase<T> {
    private final Callable<T> base;

    public NUnsafeCallableBaseFromJavaCallable(Callable<T> base) {
        this.base = base;
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.of("callable").apply(session);
    }

    @Override
    public T call(NSession session) throws Exception {
        return base.call();
    }
}
