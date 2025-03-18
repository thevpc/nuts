package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

public class NFunction3WithDescription<A, B, C, V> implements NFunction3<A, B, C, V> {
    private final NFunction3<A, B, C, V> base;
    private NEDesc description;

    public NFunction3WithDescription(NFunction3<A, B, C, V> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(A a, B b, C c) {
        return base.apply(a,b,c);
    }

    @Override
    public NFunction3<A, B, C, V> withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "Function{" + base + '}';
    }

    @Override
    public NElement describe() {
        return NEDesc.safeDescribe(
                description,
                NEDesc.ofPossibleDescribable(base),
                NEDesc.ofLateToString(this)
        );
    }
}
