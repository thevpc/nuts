package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;

public interface NutsFunction3<A, B, C, V> extends NutsDescribable {
    V apply(A a, B b, C c);

    default NutsElement describe(NutsSession session) {
        return NutsElements.of(session).toElement("custom function 3");
    }
}
