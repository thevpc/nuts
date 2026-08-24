package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * NListItemElement interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NListItemElement {
    /**
     * Marker variant.
     *
     * @return marker variant result
     */
    String markerVariant();

    /**
     * Marker.
     *
     * @return marker result
     */
    String marker();

    /**
     * Depth.
     *
     * @return depth result
     */
    int depth();

    /**
     * Affixes.
     *
     * @return affixes result
     */
    List<NBoundAffix> affixes();

    /**
     * List type.
     *
     * @return list type result
     */
    NElementType listType();

    /**
     * Value.
     *
     * @return value result
     */
    NOptional<NElement> value();

    /**
     * Sub list.
     *
     * @return sub list result
     */
    NOptional<NListElement> subList();

    /**
     * Builder.
     *
     * @return builder result
     */
    NListItemElementBuilder builder();

}
