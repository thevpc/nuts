package net.thevpc.nuts.math;

import net.thevpc.nuts.util.NOptional;

public interface NFloatComplex extends NNumber {
    NFloatComplex ZERO = NFloatComplexImpl.ZERO;
    NFloatComplex ONE = NFloatComplexImpl.ONE;
    NFloatComplex I = NFloatComplexImpl.I;

    static NFloatComplex of(String any) {
        return NFloatComplexImpl.parse(any).get();
    }

    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    static NOptional<NFloatComplex> parse(String any) {
        return NFloatComplexImpl.parse(any);
    }

    static NFloatComplex of(float x, float y) {
        return NFloatComplexImpl.of(x, y);
    }

    static NFloatComplex ofPolar(float r, float theta) {
        return NFloatComplexImpl.of(r, theta);
    }

    float realValue();

    float imagValue();

    float absFloat();

    NFloatComplex addFloatComplex(NFloatComplex other);

    NFloatComplex negateFloatComplex();

    NFloatComplex subtractFloatComplex(NFloatComplex other);

    NFloatComplex multiplyFloatComplex(NFloatComplex z2);

    NFloatComplex divideFloatComplex(NFloatComplex other);

    NFloatComplex invFloatComplex();
}
