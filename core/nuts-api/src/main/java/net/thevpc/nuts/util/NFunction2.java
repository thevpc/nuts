package net.thevpc.nuts.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.reserved.util.NFunction2WithDescription;

public interface NFunction2<A, B, V> extends NElementDescribable<NFunction2<A, B, V>> {
    V apply(A a, B b);

    @Override
    default NFunction2<A, B, V> withDesc(NEDesc description){
        if(description==null){
            return this;
        }
        return new NFunction2WithDescription<>(this,description);
    }

    default NElement describe(NSession session) {
        return NElements.of(session).toElement("custom function 2");
    }
}
