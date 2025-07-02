package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

public interface NDoubleFunction2 extends NElementRedescribable<NDoubleFunction2> {
    double apply(double x, double y);

    @Override
    default NDoubleFunction2 redescribe(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NDoubleFunction2WithDescription(this,description);
    }

    default NElement describe() {
        return NElements.of().toElement("double function 2");
    }
}
