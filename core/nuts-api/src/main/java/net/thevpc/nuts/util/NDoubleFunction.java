package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

public interface NDoubleFunction extends NRedescribable<NDoubleFunction> {
    double apply(double x);

    @Override
    default NDoubleFunction withDescription(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NDoubleFunctionWithDescription(this,description);
    }

    default NElement describe() {
        return NElements.of().toElement("custom function");
    }
}
