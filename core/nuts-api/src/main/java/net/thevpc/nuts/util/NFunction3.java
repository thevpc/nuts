package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

/**
 * NFunction3 interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NFunction3<A, B, C, V> extends NRedescribable<NFunction3<A, B, C, V>> {
    /**
     * Apply.
     *
     * @param a a
     * @param b b
     * @param c c
     * @return apply result
     */
    V apply(A a, B b, C c);

    /**
     * With description.
     *
     * @param description description
     * @return with description result
     */
    default NFunction3<A, B, C, V> withDescription(Supplier<NElement> description) {
        return description == null ? this : new NFunction3WithDescription<>(this, description);
    }

    /**
     * Describe.
     *
     * @return describe result
     */
    default NElement describe() {
        return NElement.of("custom function 3");
    }
}
