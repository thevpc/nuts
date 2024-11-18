package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NCallable;

public class NCallableWithDescription<T> implements NCallable<T> {
    private final NCallable<T> base;
    private NEDesc description;

    public NCallableWithDescription(NCallable<T> base, NEDesc description) {
        this.base = base;
        this.description = description == null ? NEDesc.of("callable") : description;
    }

    @Override
    public NElement describe() {
        return description.get();
    }

    @Override
    public NCallable<T> withDesc(NEDesc description) {
        this.description=description;
        return this;
    }

    @Override
    public T call() {
        return base.call();
    }
}
