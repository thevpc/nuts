package net.thevpc.nuts.math;

import net.thevpc.nuts.util.NOptional;

/**
 * NFloatComplex interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NFloatComplex extends NNumber {
    NFloatComplex ZERO = NFloatComplexImpl.ZERO;
    NFloatComplex ONE = NFloatComplexImpl.ONE;
    NFloatComplex I = NFloatComplexImpl.I;

    /**
     * Creates a new instance of of.
     *
     * @param any any
     * @return of result
     */
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

    /**
     * Creates a new instance of of.
     *
     * @param x x
     * @param y y
     * @return of result
     */
    static NFloatComplex of(float x, float y) {
        return NFloatComplexImpl.of(x, y);
    }

    /**
     * Creates a new instance of of polar.
     *
     * @param r r
     * @param theta theta
     * @return of polar result
     */
    static NFloatComplex ofPolar(float r, float theta) {
        return NFloatComplexImpl.of(r, theta);
    }

    /**
     * Real value.
     *
     * @return real value result
     */
    float realValue();

    /**
     * Imag value.
     *
     * @return imag value result
     */
    float imagValue();

    /**
     * Abs float.
     *
     * @return abs float result
     */
    float absFloat();

    /**
     * Adds the specified float complex.
     *
     * @param other other
     * @return add float complex result
     */
    NFloatComplex addFloatComplex(NFloatComplex other);

    /**
     * Negate float complex.
     *
     * @return negate float complex result
     */
    NFloatComplex negateFloatComplex();

    /**
     * Subtract float complex.
     *
     * @param other other
     * @return subtract float complex result
     */
    NFloatComplex subtractFloatComplex(NFloatComplex other);

    /**
     * Multiply float complex.
     *
     * @param z2 z2
     * @return multiply float complex result
     */
    NFloatComplex multiplyFloatComplex(NFloatComplex z2);

    /**
     * Divide float complex.
     *
     * @param other other
     * @return divide float complex result
     */
    NFloatComplex divideFloatComplex(NFloatComplex other);

    /**
     * Inv float complex.
     *
     * @return inv float complex result
     */
    NFloatComplex invFloatComplex();

    /**
     * Checks if is real.
     *
     * @return is real result
     */
    boolean isReal();

    /**
     * Checks if is imaginary.
     *
     * @return is imaginary result
     */
    boolean isImaginary();
}
