package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;

import java.util.function.Supplier;

public interface NDoubleFunction3 extends NElementRedescribable<NDoubleFunction3> {
    double apply(double x, double y, double z);

    @Override
    default NDoubleFunction3 redescribe(Supplier<NElement> description){
        if(description==null){
            return this;
        }
        return new NDoubleFunction3WithDescription(this,description);
    }

    default NElement describe() {
        return NElements.of().toElement("double function 3");
    }
}
