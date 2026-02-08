package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface NElementPath {
    static NElementPath ofRoot() {
        return NElements.of().createRootPath();
    }

    int size();

    /**
     * @return The specific step taken from the parent to arrive here.
     * Returns null if called on the Root.
     */
    NElementStep step();

    NOptional<NElementPath> parent();

    boolean isRoot();

    NElementPath resolve(NElementStep step);

    List<NElementStep> steps();
}
