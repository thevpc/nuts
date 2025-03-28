package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;

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
        if(description==null){
            return base;
        }
        return new NFunction2WithDescription<>(base, description);
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
