package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.elem.NElements;

public interface NDoubleFunction2 extends NElementDescribable<NDoubleFunction2> {
    double apply(double x, double y);

    @Override
    default NDoubleFunction2 withDesc(NEDesc description){
        if(description==null){
            return this;
        }
        return new NDoubleFunction2WithDescription(this,description);
    }

    default NElement describe() {
        return NElements.of().toElement("double function 2");
    }
}
