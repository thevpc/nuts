package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.util.NOptional;

/**
 * NElementNavigator interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementNavigator {
    /**
     * Creates a new instance of of root.
     *
     * @param element element
     * @return of root result
     */
    static NElementNavigator ofRoot(NElement element) {
        return NElementRPI.of().createRootNavigator(element);
    }

    /**
     * Parent.
     *
     * @return parent result
     */
    NOptional<NElementNavigator> parent();

    /**
     * Element.
     *
     * @return element result
     */
    NElement element();

    /**
     * Path.
     *
     * @return path result
     */
    NElementPath path();

    /**
     * Resolve.
     *
     * @param step step
     * @return resolve result
     */
    NOptional<NElementNavigator> resolve(NElementStep step);

    /**
     * Resolve.
     *
     * @param path path
     * @return resolve result
     */
    NOptional<NElementNavigator> resolve(NElementPath path);
}
