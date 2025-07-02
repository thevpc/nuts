package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NUnsafeFunctionWithDescription<T, V> implements NUnsafeFunction<T, V>, NImmutable {
    private final NUnsafeFunction<T, V> base;
    private final Supplier<NElement> description;

    public NUnsafeFunctionWithDescription(NUnsafeFunction<T, V> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) throws Exception {
        return base.apply(f);
    }

    @Override
    public NUnsafeFunction<T, V> redescribe(Supplier<NElement> description) {
        if (description == null) {
            return base;
        }
        return new NUnsafeFunctionWithDescription<>(base, description);
    }

    @Override
    public String toString() {
        return "UnsafeFunction{" + base + '}';
    }

    @Override
    public NElement describe() {
        return NDescribableElementSupplier.safeDescribe(description, NDescribableElementSupplier.ofLateToString(this));
    }
}
