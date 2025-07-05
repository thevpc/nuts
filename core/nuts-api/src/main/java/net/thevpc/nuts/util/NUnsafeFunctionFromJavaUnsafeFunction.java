package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NUnsafeFunctionFromJavaUnsafeFunction<T, V> implements NUnsafeFunction<T, V>, NImmutable {
    private final UnsafeFunction<T, V> base;
    private final Supplier<NElement> description;

    public NUnsafeFunctionFromJavaUnsafeFunction(UnsafeFunction<T, V> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) throws Exception {
        return base.apply(f);
    }

    @Override
    public NUnsafeFunction<T, V> redescribe(Supplier<NElement> description) {
        return new NUnsafeFunctionFromJavaUnsafeFunction<>(base, description);
    }

    @Override
    public String toString() {
        return "UnsafeFunction{" + base + '}';
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribe(description, NDescribables.ofLateToString(this));
    }
}
