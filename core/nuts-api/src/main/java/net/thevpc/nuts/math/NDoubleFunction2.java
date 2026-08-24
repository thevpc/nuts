package net.thevpc.nuts.math;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

/**
 * NDoubleFunction2 interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDoubleFunction2 extends NRedescribable<NDoubleFunction2> {
    /**
     * Apply.
     *
     * @param x x
     * @param y y
     * @return apply result
     */
    double apply(double x, double y);

    @Override
    default NDoubleFunction2 withDescription(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NDoubleFunction2WithDescription(this,description);
    }

    /**
     * Describe.
     *
     * @return describe result
     */
    default NElement describe() {
        return NElement.of("double function 2");
    }
}
