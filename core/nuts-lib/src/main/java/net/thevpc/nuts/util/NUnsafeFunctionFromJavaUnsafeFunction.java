package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

public class NUnsafeFunctionFromJavaUnsafeFunction<T, V> implements NUnsafeFunction<T, V>, NImmutable {
    private final UnsafeFunction<T, V> base;
    private final NEDesc description;

    public NUnsafeFunctionFromJavaUnsafeFunction(UnsafeFunction<T, V> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) throws Exception {
        return base.apply(f);
    }

    @Override
    public NUnsafeFunction<T, V> withDesc(NEDesc description) {
        return new NUnsafeFunctionFromJavaUnsafeFunction<>(base, description);
    }

    @Override
    public String toString() {
        return "UnsafeFunction{" + base + '}';
    }

    @Override
    public NElement describe() {
        return NEDesc.safeDescribe(description, NEDesc.ofLateToString(this));
    }
}
