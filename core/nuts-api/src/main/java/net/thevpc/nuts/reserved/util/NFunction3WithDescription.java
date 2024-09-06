package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFunction3;

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
    public NElement describe(NSession session) {
        return NEDesc.safeDescribe(session,
                description,
                NEDesc.ofPossibleDescribable(base),
                NEDesc.ofLateToString(this)
        );
    }
}
