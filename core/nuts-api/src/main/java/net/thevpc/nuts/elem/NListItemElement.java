package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

public interface NListItemElement {
    int depth();
    NOptional<NElement> value();
    NOptional<NListElement> subList();
}
