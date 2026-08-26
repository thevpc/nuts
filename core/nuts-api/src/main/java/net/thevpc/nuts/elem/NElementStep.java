package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;

/**
 * NElementStep interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementStep extends Serializable, NToElement {
    /**
     * Creates a new instance of of child.
     *
     * @param index index
     * @return of child result
     */
    static NElementStep ofChild(int index) {
        return NElementRPI.of().createStepChild(index);
    }

    /**
     * Creates a new instance of of child.
     *
     * @param name name
     * @return of child result
     */
    static NElementStep ofChild(String name) {
        return NElementRPI.of().createStepChild(name);
    }

    /**
     * Creates a new instance of of param.
     *
     * @param index index
     * @return of param result
     */
    static NElementStep ofParam(int index) {
        return NElementRPI.of().createStepParam(index);
    }

    /**
     * Creates a new instance of of param.
     *
     * @param name name
     * @return of param result
     */
    static NElementStep ofParam(String name) {
        return NElementRPI.of().createStepParam(name);
    }

    /**
     * Creates a new instance of of ann param.
     *
     * @param paramIndex param index
     * @param index index
     * @return of ann param result
     */
    static NElementStep ofAnnParam(int paramIndex, int index) {
        return NElementRPI.of().createStepAnnotationParam(paramIndex, index);
    }

    /**
     * Creates a new instance of of ann param.
     *
     * @param paramIndex param index
     * @param name name
     * @return of ann param result
     */
    static NElementStep ofAnnParam(int paramIndex, String name) {
        return NElementRPI.of().createStepAnnotationParam(paramIndex, name);
    }

    /**
     * Creates a new instance of of sub list.
     *
     * @param index index
     * @return of sub list result
     */
    static NElementStep ofSubList(int index) {
        return NElementRPI.of().createStepSubList(index);
    }

    /**
     * Step.
     *
     * @param element element
     * @return step result
     */
    NOptional<NElement> step(NElement element);
}
