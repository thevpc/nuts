package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;

public interface NElementStep extends Serializable, NToElement {
    static NElementStep ofChild(int index) {
        return NElements.of().createStepChild(index);
    }

    static NElementStep ofChild(String name) {
        return NElements.of().createStepChild(name);
    }

    static NElementStep ofParam(int index) {
        return NElements.of().createStepParam(index);
    }

    static NElementStep ofParam(String name) {
        return NElements.of().createStepParam(name);
    }

    static NElementStep ofAnnParam(int paramIndex, int index) {
        return NElements.of().createStepAnnotationParam(paramIndex, index);
    }

    static NElementStep ofAnnParam(int paramIndex, String name) {
        return NElements.of().createStepAnnotationParam(paramIndex, name);
    }

    static NElementStep ofSubList(int index) {
        return NElements.of().createStepSubList(index);
    }

    NOptional<NElement> step(NElement element);
}
