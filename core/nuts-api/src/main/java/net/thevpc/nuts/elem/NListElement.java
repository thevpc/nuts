package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * element type (NElementType) is either ORDERED_LIST, or UnORDERED_LIST
 */
public interface NListElement extends NElement {
    int depth();

    int size();

    List<NListItemElement> items();

    NOptional<NListItemElement> get(int index);

    NListElementBuilder builder();
}
