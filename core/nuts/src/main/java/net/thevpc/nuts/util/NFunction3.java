package net.thevpc.nuts.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

public interface NFunction3<A, B, C, V> extends NDescribable {
    V apply(A a, B b, C c);

    default NElement describe(NSession session) {
        return NElements.of(session).toElement("custom function 3");
    }
}
