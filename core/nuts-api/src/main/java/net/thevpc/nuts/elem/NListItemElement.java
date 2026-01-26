package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface NListItemElement {
    String markerVariant();

    String marker();

    int depth();

    List<NBoundAffix> affixes();

    NElementType listType();

    NOptional<NElement> value();

    NOptional<NListElement> subList();

    NListItemElementBuilder builder();

}
