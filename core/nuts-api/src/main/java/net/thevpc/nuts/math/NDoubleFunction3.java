package net.thevpc.nuts.math;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

/**
 * NDoubleFunction3 interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDoubleFunction3 extends NRedescribable<NDoubleFunction3> {
    /**
     * Apply.
     *
     * @param x x
     * @param y y
     * @param z z
     * @return apply result
     */
    double apply(double x, double y, double z);

    @Override
    default NDoubleFunction3 withDescription(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NDoubleFunction3WithDescription(this,description);
    }

    /**
     * Describe.
     *
     * @return describe result
     */
    default NElement describe() {
        return NElement.of("double function 3");
    }
}
