package net.thevpc.nuts.math;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NImmutable;

import java.util.function.Supplier;

/**
 * NDoubleFunctionWithDescription class.
 *
 * @author thevpc
 * @since 0.8.0
 */
@NImmutable
public class NDoubleFunctionWithDescription implements NDoubleFunction{
    private final NDoubleFunction base;
    private final Supplier<NElement> description;

    /**
     * N double function with description.
     *
     * @param base base
     * @param description description
     * @return n double function with description result
     */
    public NDoubleFunctionWithDescription(NDoubleFunction base, Supplier<NElement> description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public double apply(double x) {
        return base.apply(x);
    }

    @Override
    public NDoubleFunction withDescription(Supplier<NElement> description) {
        if(description==null){
            return base;
        }
        return new NDoubleFunctionWithDescription(base, description);
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
