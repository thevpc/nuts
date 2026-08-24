package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

/**
 * NNamedElement interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NNamedElement extends NElement {
    /**
     * Name.
     *
     * @return name result
     */
    NOptional<String> name();
}
