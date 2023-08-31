package net.thevpc.nuts.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

public interface NFunction2<A, B, V> extends NDescribable {
    V apply(A a, B b);

    default NElement describe(NSession session) {
        return NElements.of(session).toElement("custom function 2");
    }
}
