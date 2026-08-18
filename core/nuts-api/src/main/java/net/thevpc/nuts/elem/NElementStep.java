package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;

public interface NElementStep extends Serializable, NToElement {
    static NElementStep ofChild(int index) {
        return NElementRPI.of().createStepChild(index);
    }

    static NElementStep ofChild(String name) {
        return NElementRPI.of().createStepChild(name);
    }

    static NElementStep ofParam(int index) {
        return NElementRPI.of().createStepParam(index);
    }

    static NElementStep ofParam(String name) {
        return NElementRPI.of().createStepParam(name);
    }

    static NElementStep ofAnnParam(int paramIndex, int index) {
        return NElementRPI.of().createStepAnnotationParam(paramIndex, index);
    }

    static NElementStep ofAnnParam(int paramIndex, String name) {
        return NElementRPI.of().createStepAnnotationParam(paramIndex, name);
    }

    static NElementStep ofSubList(int index) {
        return NElementRPI.of().createStepSubList(index);
    }

    NOptional<NElement> step(NElement element);
}
