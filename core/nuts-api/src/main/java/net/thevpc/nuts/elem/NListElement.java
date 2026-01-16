package net.thevpc.nuts.elem;

import java.util.List;

/**
 * element type (NElementType) is either ORDERED_LIST, or UnORDERED_LIST
 */
public interface NListElement extends NElement{
    int depth();
    int size();
    List<NListItemElement> children();
}
