package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.function.Supplier;

public class NCallableWithDescription<T> implements NCallable<T> {
    private final NCallable<T> base;
    private Supplier<NElement> description;

    public NCallableWithDescription(NCallable<T> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description == null ? NElementDescribables.ofDesc("callable") : description;
    }

    @Override
    public NElement describe() {
        return description.get();
    }

    @Override
    public NCallable<T> redescribe(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public T call() {
        return base.call();
    }
}
