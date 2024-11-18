package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NUnsafeFunction;
import net.thevpc.nuts.util.UnsafeFunction;

public class NUnsafeFunctionFromJavaUnsafeFunction<T, V> implements NUnsafeFunction<T, V> {
    private final UnsafeFunction<? super T, V> base;
    private NEDesc description;

    public NUnsafeFunctionFromJavaUnsafeFunction(UnsafeFunction<? super T, V> base) {
        this.base = base;
    }

    @Override
    public V apply(T f) throws Exception {
        return base.apply(f);
    }

    @Override
    public NUnsafeFunctionFromJavaUnsafeFunction<T, V> withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "UnsafeFunction{" + base + '}';
    }

    @Override
    public NElement describe() {
        return NEDesc.safeDescribe( description, NEDesc.ofLateToString(this));
    }
}
