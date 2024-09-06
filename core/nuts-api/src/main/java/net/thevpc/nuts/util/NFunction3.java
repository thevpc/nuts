package net.thevpc.nuts.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.reserved.util.NFunction3WithDescription;

public interface NFunction3<A, B, C, V> extends NElementDescribable<NFunction3<A, B, C, V>> {
    V apply(A a, B b, C c);

    default NFunction3<A, B, C, V> withDesc(NEDesc description){
        return new NFunction3WithDescription<>(this,description);
    }

    default NElement describe(NSession session) {
        return NElements.of(session).toElement("custom function 3");
    }
}
