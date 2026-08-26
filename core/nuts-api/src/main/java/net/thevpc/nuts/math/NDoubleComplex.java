package net.thevpc.nuts.math;

import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;

/**
 * NDoubleComplex interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
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
        /**
         * Parse.
         *
         * @param any).get( any).get(
         * @return parse result
         */
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

    /**
     * Creates a new instance of of.
     *
     * @param x x
     * @param y y
     * @return of result
     */
    static NDoubleComplex of(double x, double y) {
        return NDoubleComplexImpl.of(x, y);
    }

    /**
     * Creates a new instance of of polar.
     *
     * @param r r
     * @param theta theta
     * @return of polar result
     */
    static NDoubleComplex ofPolar(double r, double theta) {
        return NDoubleComplexImpl.ofPolar(r, theta);
    }

    /**
     * Real value.
     *
     * @return real value result
     */
    double realValue();

    /**
     * Imag value.
     *
     * @return imag value result
     */
    double imagValue();

    /**
     * Abs double.
     *
     * @return abs double result
     */
    double absDouble();

    /**
     * Double value.
     *
     * @return double value result
     */
    double doubleValue();

    /**
     * Int value.
     *
     * @return int value result
     */
    int intValue();

    /**
     * Long value.
     *
     * @return long value result
     */
    long longValue();

    /**
     * Float value.
     *
     * @return float value result
     */
    float floatValue();

    /**
     * method name is on pupose too long and contains DoubleComplex
     * to avoid implementation existing methods
     *
     * @param other complex
     * @return complex
     */
    NDoubleComplex addDoubleComplex(NDoubleComplex other);

    /**
     * Negate double complex.
     *
     * @return negate double complex result
     */
    NDoubleComplex negateDoubleComplex();

    /**
     * Subtract double complex.
     *
     * @param other other
     * @return subtract double complex result
     */
    NDoubleComplex subtractDoubleComplex(NDoubleComplex other);

    /**
     * Multiply double complex.
     *
     * @param z2 z2
     * @return multiply double complex result
     */
    NDoubleComplex multiplyDoubleComplex(NDoubleComplex z2);

    /**
     * Divide double complex.
     *
     * @param other other
     * @return divide double complex result
     */
    NDoubleComplex divideDoubleComplex(NDoubleComplex other);

    /**
     * Inv double complex.
     *
     * @return inv double complex result
     */
    NDoubleComplex invDoubleComplex();

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
