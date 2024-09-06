package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NUnsafeCallableBase;
import net.thevpc.nuts.util.NUnsafeCallable;

public class NUnsafeCallableWithDescription<T> extends NUnsafeCallableBase<T> {
    private final NUnsafeCallable<T> base;
    private NEDesc description;

    public NUnsafeCallableWithDescription(NUnsafeCallable<T> base, NEDesc description) {
        this.base = base;
        this.description = description == null ? NEDesc.of("callable") : description;
    }

    @Override
    public NElement describe(NSession session) {
        return description.apply(session);
    }

    @Override
    public NUnsafeCallable<T> withDesc(NEDesc description) {
        this.description=description;
        return this;
    }

    @Override
    public T call(NSession session) throws Exception {
        return base.call(session);
    }
}
