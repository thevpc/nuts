package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NFunction2WithDescription<A,B,V> implements NFunction2<A,B,V> {
    private final NFunction2<A,B,V> base;
    private Supplier<NElement> description;

    public NFunction2WithDescription(NFunction2<A,B,V> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(A a,B b) {
        return base.apply(a,b);
    }

    @Override
    public NFunction2<A,B, V> redescribe(Supplier<NElement> description) {
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
        return NDescribableElementSupplier.safeDescribe(
                description,
                NDescribableElementSupplier.ofPossibleDescribable(base),
                NDescribableElementSupplier.ofLateToString(this)
        );
    }
}
