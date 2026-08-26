package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Supplier;

/**
 * NFunction3WithDescription class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NFunction3WithDescription<A, B, C, V> implements NFunction3<A, B, C, V> {
    private final NFunction3<A, B, C, V> base;
    private Supplier<NElement> description;

    /**
     * N function3 with description.
     *
     * @param base base
     * @param description description
     * @return n function3 with description result
     */
    public NFunction3WithDescription(NFunction3<A, B, C, V> base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public V apply(A a, B b, C c) {
        return base.apply(a,b,c);
    }

    @Override
    public NFunction3<A, B, C, V> withDescription(Supplier<NElement> description) {
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
        return NDescribables.safeDescribe(
                description,
                NDescribables.ofDesc(base),
                NDescribables.ofLateToString(this)
        );
    }
}
