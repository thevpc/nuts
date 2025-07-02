package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

public interface NFunction2<A, B, V> extends NElementRedescribable<NFunction2<A, B, V>> {
    V apply(A a, B b);

    @Override
    default NFunction2<A, B, V> redescribe(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NFunction2WithDescription<>(this,description);
    }

    default NElement describe() {
        return NElements.of().toElement("custom function 2");
    }
}
