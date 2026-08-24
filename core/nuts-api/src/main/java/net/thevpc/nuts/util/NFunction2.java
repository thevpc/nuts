package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

/**
 * NFunction2 interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NFunction2<A, B, V> extends NRedescribable<NFunction2<A, B, V>> {
    /**
     * Apply.
     *
     * @param a a
     * @param b b
     * @return apply result
     */
    V apply(A a, B b);

    @Override
    default NFunction2<A, B, V> withDescription(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NFunction2WithDescription<>(this,description);
    }

    /**
     * Describe.
     *
     * @return describe result
     */
    default NElement describe() {
        return NElement.of("custom function 2");
    }
}
