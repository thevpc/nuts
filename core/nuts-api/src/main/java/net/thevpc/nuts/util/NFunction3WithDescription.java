package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

public class NFunction3WithDescription<A, B, C, V> implements NFunction3<A, B, C, V> {
    private final NFunction3<A, B, C, V> base;
    private Supplier<NElement> description;

    public NFunction3WithDescription(NFunction3<A, B, C, V> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(A a, B b, C c) {
        return base.apply(a,b,c);
    }

    @Override
    public NFunction3<A, B, C, V> redescribe(Supplier<NElement> description) {
        if(description==null){
            return base;
        }
        return new NFunction3WithDescription<>(base, description);
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
