package net.thevpc.nuts.math;

import net.thevpc.nuts.util.NOptional;

import java.math.BigDecimal;
import java.math.MathContext;

public interface NBigComplex extends NNumber {
    NBigComplex ZERO = NBigComplexImpl.ZERO;
    NBigComplex ONE = NBigComplexImpl.ONE;
    NBigComplex I = NBigComplexImpl.I;

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

    static NBigComplex of(BigDecimal x, BigDecimal y) {
        return NBigComplexImpl.of(x,y);
    }

    static NBigComplex ofPolar(BigDecimal r, double theta) {
        return NBigComplexImpl.ofPolar(r,theta);
    }

    BigDecimal real();

    BigDecimal imag();


    NBigComplex addBigComplex(NBigComplex other);


    NBigComplex negateBigComplex();

    NBigComplex subtractBigComplex(NBigComplex other) ;

    NBigComplex multiplyBigComplex(NBigComplex z2, MathContext mc);

    NBigComplex divideBigComplex(NBigComplex other, MathContext mc);

    NBigComplex invBigComplex(MathContext mc) ;
}
