package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

public class NUnsafeFunctionWithDescription<T, V> implements NUnsafeFunction<T, V>, NImmutable {
    private final NUnsafeFunction<T, V> base;
    private final NEDesc description;

    public NUnsafeFunctionWithDescription(NUnsafeFunction<T, V> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) throws Exception {
        return base.apply(f);
    }

    @Override
    public NUnsafeFunction<T, V> withDesc(NEDesc description) {
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
        return NEDesc.safeDescribe(description, NEDesc.ofLateToString(this));
    }
}
