package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * NElementPath interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementPath {
    /**
     * Creates a new instance of of root.
     *
     * @return of root result
     */
    static NElementPath ofRoot() {
        return NElementRPI.of().createRootPath();
    }

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * @return The specific step taken from the parent to arrive here.
     * Returns null if called on the Root.
     */
    NElementStep step();

    /**
     * Parent.
     *
     * @return parent result
     */
    NOptional<NElementPath> parent();

    /**
     * Checks if is root.
     *
     * @return is root result
     */
    boolean isRoot();

    /**
     * Resolve.
     *
     * @param step step
     * @return resolve result
     */
    NElementPath resolve(NElementStep step);

    /**
     * Steps.
     *
     * @return steps result
     */
    List<NElementStep> steps();
}
