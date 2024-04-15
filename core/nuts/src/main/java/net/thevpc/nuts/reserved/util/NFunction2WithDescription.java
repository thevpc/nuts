package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFunction2;

public class NFunction2WithDescription<A,B,V> implements NFunction2<A,B,V> {
    private final NFunction2<A,B,V> base;
    private NEDesc description;

    public NFunction2WithDescription(NFunction2<A,B,V> base, NEDesc description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(A a,B b) {
        return base.apply(a,b);
    }

    @Override
    public NFunction2<A,B, V> withDesc(NEDesc description) {
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
