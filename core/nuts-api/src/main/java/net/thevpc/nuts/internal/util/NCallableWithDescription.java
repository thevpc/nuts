package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.function.Supplier;

/**
 * NCallableWithDescription class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCallableWithDescription<T> implements NCallable<T> {
    private final NCallable<T> base;
    private Supplier<NElement> description;

    /**
     * N callable with description.
     *
     * @param base base
     * @param description description
     * @return n callable with description result
     */
    public NCallableWithDescription(NCallable<T> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description == null ? NDescribables.ofDesc("callable") : description;
    }

    @Override
    public NElement describe() {
        return description.get();
    }

    @Override
    public NCallable<T> withDescription(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public T call() {
        return base.call();
    }
}
