package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

public interface NDoubleFunction extends NElementRedescribable<NDoubleFunction> {
    double apply(double x);

    @Override
    default NDoubleFunction redescribe(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NDoubleFunctionWithDescription(this,description);
    }

    default NElement describe() {
        return NElements.of().toElement("custom function");
    }
}
