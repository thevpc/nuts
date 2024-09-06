package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NUnsafeFunction;

public class NUnsafeFunctionWithDescription<T, V> implements NUnsafeFunction<T, V> {
    private final NUnsafeFunction<? super T, V> base;
    private NEDesc description;

    public NUnsafeFunctionWithDescription(NUnsafeFunction<? super T, V> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(T f) throws Exception {
        return base.apply(f);
    }

    @Override
    public NUnsafeFunctionWithDescription<T, V> withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "UnsafeFunction{" + base + '}';
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.safeDescribe(session, description, NEDesc.ofLateToString(this));
    }
}
