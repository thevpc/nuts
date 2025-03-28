package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.elem.NElements;

public interface NDoubleFunction3 extends NElementDescribable<NDoubleFunction3> {
    double apply(double x, double y, double z);

    @Override
    default NDoubleFunction3 withDesc(NEDesc description){
        if(description==null){
            return this;
        }
        return new NDoubleFunction3WithDescription(this,description);
    }

    default NElement describe() {
        return NElements.of().toElement("double function 3");
    }
}
