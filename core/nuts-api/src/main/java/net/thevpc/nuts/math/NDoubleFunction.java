package net.thevpc.nuts.math;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

/**
 * NDoubleFunction interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDoubleFunction extends NRedescribable<NDoubleFunction> {
    /**
     * Apply.
     *
     * @param x x
     * @return apply result
     */
    double apply(double x);

    @Override
    default NDoubleFunction withDescription(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NDoubleFunctionWithDescription(this,description);
    }

    /**
     * Describe.
     *
     * @return describe result
     */
    default NElement describe() {
        return NElement.of("custom function");
    }
}
