package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NUnsafeCallableBase;
import net.thevpc.nuts.util.NUnsafeCallable;

import java.util.function.Supplier;

public class NUnsafeCallableWithDescription<T> extends NUnsafeCallableBase<T> {
    private final NUnsafeCallable<T> base;
    private Supplier<NElement> description;

    public NUnsafeCallableWithDescription(NUnsafeCallable<T> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description == null ? NDescribableElementSupplier.of("callable") : description;
    }

    @Override
    public NElement describe() {
        return description.get();
    }

    @Override
    public NUnsafeCallable<T> redescribe(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public T call() throws Exception {
        return base.call();
    }
}
