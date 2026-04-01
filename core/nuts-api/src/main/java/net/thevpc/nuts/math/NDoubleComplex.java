package net.thevpc.nuts.math;

import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;

public interface NDoubleComplex extends Serializable, NNumber {
    NDoubleComplex ZERO = NDoubleComplexImpl.ZERO;
    NDoubleComplex ONE = NDoubleComplexImpl.ONE;
    NDoubleComplex I = NDoubleComplexImpl.I;

    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    static NDoubleComplex of(String any) {
        return parse(any).get();
    }

    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    static NOptional<NDoubleComplex> parse(String any) {
        return NDoubleComplexImpl.parse(any);
    }

    static NDoubleComplex of(double x, double y) {
        return NDoubleComplexImpl.of(x, y);
    }

    static NDoubleComplex ofPolar(double r, double theta) {
        return NDoubleComplexImpl.ofPolar(r, theta);
    }

    double realValue();

    double imagValue();

    double absDouble();

    double doubleValue();

    int intValue();

    long longValue();

    float floatValue();

    /**
     * method name is on pupose too long and contains DoubleComplex
     * to avoid implementation existing methods
     *
     * @param other complex
     * @return complex
     */
    NDoubleComplex addDoubleComplex(NDoubleComplex other);

    NDoubleComplex negateDoubleComplex();

    NDoubleComplex subtractDoubleComplex(NDoubleComplex other);

    NDoubleComplex multiplyDoubleComplex(NDoubleComplex z2);

    NDoubleComplex divideDoubleComplex(NDoubleComplex other);

    NDoubleComplex invDoubleComplex();

    boolean isReal();

    boolean isImaginary();
}
