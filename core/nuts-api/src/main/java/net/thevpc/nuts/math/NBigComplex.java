package net.thevpc.nuts.math;

import net.thevpc.nuts.util.NOptional;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * NBigComplex interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NBigComplex extends NNumber {
    NBigComplex ZERO = NBigComplexImpl.ZERO;
    NBigComplex ONE = NBigComplexImpl.ONE;
    NBigComplex I = NBigComplexImpl.I;

    /**
     * Creates a new instance of of.
     *
     * @param any any
     * @return of result
     */
    static NBigComplex of(String any) {
        return NBigComplexImpl.of(any);
    }
    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    static NOptional<NBigComplex> parse(String any) {
        return NBigComplexImpl.parse(any);
    }

    /**
     * Creates a new instance of of.
     *
     * @param x x
     * @param y y
     * @return of result
     */
    static NBigComplex of(BigDecimal x, BigDecimal y) {
        return NBigComplexImpl.of(x,y);
    }

    /**
     * Creates a new instance of of polar.
     *
     * @param r r
     * @param theta theta
     * @return of polar result
     */
    static NBigComplex ofPolar(BigDecimal r, double theta) {
        return NBigComplexImpl.ofPolar(r,theta);
    }

    /**
     * Real value.
     *
     * @return real value result
     */
    BigDecimal realValue();

    /**
     * Imag value.
     *
     * @return imag value result
     */
    BigDecimal imagValue();


    /**
     * Adds the specified big complex.
     *
     * @param other other
     * @return add big complex result
     */
    NBigComplex addBigComplex(NBigComplex other);


    /**
     * Negate big complex.
     *
     * @return negate big complex result
     */
    NBigComplex negateBigComplex();

    /**
     * Subtract big complex.
     *
     * @param other other
     * @return subtract big complex result
     */
    NBigComplex subtractBigComplex(NBigComplex other) ;

    /**
     * Multiply big complex.
     *
     * @param z2 z2
     * @param mc mc
     * @return multiply big complex result
     */
    NBigComplex multiplyBigComplex(NBigComplex z2, MathContext mc);

    /**
     * Divide big complex.
     *
     * @param other other
     * @param mc mc
     * @return divide big complex result
     */
    NBigComplex divideBigComplex(NBigComplex other, MathContext mc);

    /**
     * Inv big complex.
     *
     * @param mc mc
     * @return inv big complex result
     */
    NBigComplex invBigComplex(MathContext mc) ;

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
