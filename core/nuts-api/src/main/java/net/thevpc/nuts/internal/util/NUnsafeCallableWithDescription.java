package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NUnsafeCallableBase;
import net.thevpc.nuts.concurrent.NUnsafeCallable;

import java.util.function.Supplier;

/**
 * NUnsafeCallableWithDescription class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NUnsafeCallableWithDescription<T> extends NUnsafeCallableBase<T> {
    private final NUnsafeCallable<T> base;
    private Supplier<NElement> description;

    /**
     * N unsafe callable with description.
     *
     * @param base base
     * @param description description
     * @return n unsafe callable with description result
     */
    public NUnsafeCallableWithDescription(NUnsafeCallable<T> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description == null ? NDescribables.ofDesc("callable") : description;
    }

    @Override
    public NElement describe() {
        return description.get();
    }

    @Override
    public NUnsafeCallable<T> withDescription(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public T call() throws Exception {
        return base.call();
    }
}
