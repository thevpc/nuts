package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

public interface NNamedElement extends NElement {
    NOptional<String> name();
}
