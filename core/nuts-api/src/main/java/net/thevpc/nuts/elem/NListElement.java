package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * element type (NElementType) is either ORDERED_LIST, or UnORDERED_LIST
 */
public interface NListElement extends NElement {
    /**
     * Depth.
     *
     * @return depth result
     */
    int depth();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Items.
     *
     * @return items result
     */
    List<NListItemElement> items();

    /**
     * Returns the get.
     *
     * @param index index
     * @return get result
     */
    NOptional<NListItemElement> get(int index);

    /**
     * Builder.
     *
     * @return builder result
     */
    NListElementBuilder builder();
}
