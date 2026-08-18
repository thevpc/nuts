package net.thevpc.nuts.math;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

public interface NDoubleFunction2 extends NRedescribable<NDoubleFunction2> {
    double apply(double x, double y);

    @Override
    default NDoubleFunction2 withDescription(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NDoubleFunction2WithDescription(this,description);
    }

    default NElement describe() {
        return NElement.of("double function 2");
    }
}
