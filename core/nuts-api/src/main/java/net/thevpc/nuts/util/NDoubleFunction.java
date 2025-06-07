package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.elem.NElements;

public interface NDoubleFunction extends NElementDescribable<NDoubleFunction> {
    double apply(double x);

    @Override
    default NDoubleFunction withDesc(NEDesc description){
        if(description==null){
            return this;
        }
        return new NDoubleFunctionWithDescription(this,description);
    }

    default NElement describe() {
        return NElements.of().toElement("custom function");
    }
}
