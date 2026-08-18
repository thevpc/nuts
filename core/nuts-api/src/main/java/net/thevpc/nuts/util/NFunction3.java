package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

public interface NFunction3<A, B, C, V> extends NRedescribable<NFunction3<A, B, C, V>> {
    V apply(A a, B b, C c);

    default NFunction3<A, B, C, V> withDescription(Supplier<NElement> description) {
        return description == null ? this : new NFunction3WithDescription<>(this, description);
    }

    default NElement describe() {
        return NElement.of("custom function 3");
    }
}
