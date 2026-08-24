package net.thevpc.nuts.math;

import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * NBigComplexImpl class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NBigComplexImpl extends Number implements NBigComplex{
    private static final BigDecimal BIG_DECIMAL_MINUS_ONE = new BigDecimal("-1");
    public static final NBigComplex ZERO = new NBigComplexImpl(BigDecimal.ZERO, BigDecimal.ZERO);
    public static final NBigComplex ONE = new NBigComplexImpl(BigDecimal.ONE, BigDecimal.ZERO);
    public static final NBigComplex I = new NBigComplexImpl(BigDecimal.ZERO, BigDecimal.ONE);
    private BigDecimal real;
    private BigDecimal imag;

    /**
     * Creates a new instance of of.
     *
     * @param any any
     * @return of result
     */
    public static NBigComplex of(String any) {
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
    public static NOptional<NBigComplex> parse(String any) {
        try {
            if (NBlankable.isBlank(any)) {
                return NOptional.ofNamedEmpty("complex");
            }
            any = NStringUtils.strip(any);
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NBigComplexImpl(new BigDecimal(c[0]), new BigDecimal(c[1])));
        } catch (Exception e) {
            return NOptional.ofNamedError("complex : " + any);
        }
    }

    /**
     * Creates a new instance of of.
     *
     * @param x x
     * @param y y
     * @return of result
     */
    public static NBigComplex of(BigDecimal x, BigDecimal y) {
        return new NBigComplexImpl(x, y);
    }

    /**
     * Creates a new instance of of polar.
     *
     * @param r r
     * @param theta theta
     * @return of polar result
     */
    public static NBigComplex ofPolar(BigDecimal r, double theta) {
        return new NBigComplexImpl(r.multiply(new BigDecimal(Math.cos(theta))), r.multiply(new BigDecimal(Math.sin(theta))));
    }

    /**
     * N big complex impl.
     *
     * @param real real
     * @param imag imag
     * @return n big complex impl result
     */
    public NBigComplexImpl(BigDecimal real, BigDecimal imag) {
        this.real = NAssert.requireNamedNonNull(real, "real");
        this.imag = NAssert.requireNamedNonNull(imag, "imag");
    }


    @Override
    public boolean isReal() {
        return imag.equals(BigDecimal.ZERO);
    }

    @Override
    public boolean isImaginary() {
        return real.equals(BigDecimal.ZERO) && !imag.equals(BigDecimal.ZERO);
    }


    @Override
    public Number numberValue() {
        return this;
    }

    /**
     * Real value.
     *
     * @return real value result
     */
    public BigDecimal realValue() {
        return real;
    }

    /**
     * Imag value.
     *
     * @return imag value result
     */
    public BigDecimal imagValue() {
        return imag;
    }

    @Override
    public double doubleValue() {
        /**
         * Real value.
         *
         * @param ).doubleValue( ).double value(
         * @return real value result
         */
        return realValue().doubleValue();
    }

    @Override
    public int intValue() {
      /**
       * Return.
       *
       * @param doubleValue( double value(
       */
        return (int) doubleValue();
    }

    @Override
    public long longValue() {
      /**
       * Return.
       *
       * @param doubleValue( double value(
       */
        return (long) doubleValue();
    }

    @Override
    public float floatValue() {
      /**
       * Return.
       *
       * @param doubleValue( double value(
       */
        return (float) doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NBigComplexImpl ndComplex = (NBigComplexImpl) o;
        return real.compareTo(ndComplex.real) == 0 && imag.compareTo(ndComplex.imag) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(real, imag);
    }

    @Override
    public String toString() {
        if (imag.equals(BigDecimal.ZERO)) {
            /**
             * Real to string.
             *
             * @param real real
             * @return real to string result
             */
            return realToString(real);
        } else if (real.equals(BigDecimal.ZERO)) {
            /**
             * Imag to string.
             *
             * @param imag imag
             * @return imag to string result
             */
            return imagToString(imag);
        } else {
            if (imag.compareTo(BigDecimal.ZERO) < 0) {
                /**
                 * Real to string.
                 *
                 * @param imagToString(imag imag to string(imag
                 * @return real to string result
                 */
                return realToString(real) + imagToString(imag);
            }
            /**
             * Real to string.
             *
             * @param imagToString(imag imag to string(imag
             * @return real to string result
             */
            return realToString(real) + "+" + imagToString(imag);
        }
    }

    /**
     * Real to string.
     *
     * @param d d
     * @return real to string result
     */
    protected String realToString(BigDecimal d) {
        return String.valueOf(d);
    }

    /**
     * Imag to string.
     *
     * @param d d
     * @return imag to string result
     */
    protected String imagToString(BigDecimal d) {
        if (d.equals(BigDecimal.ONE)) {
            return "î";
        }
        if (d.equals(BIG_DECIMAL_MINUS_ONE)) {
            return "-î";
        }
        return d + "î";
    }

    /**
     * Compare to.
     *
     * @param other other
     * @return compare to result
     */
    public int compareTo(NBigComplex other) {
        BigDecimal oreal = other.realValue();
        BigDecimal oimag = other.imagValue();

        BigDecimal mag1 = real.multiply(real).add(imag.multiply(imag));
        BigDecimal mag2 = oreal.multiply(oreal).add(oimag.multiply(oimag));
        int cmp = mag1.compareTo(mag2);
        if (cmp != 0) return cmp;

        cmp = real.compareTo(oreal);
        if (cmp != 0) return cmp;

        return imag.compareTo(oimag);
    }

    /**
     * Adds the specified big complex.
     *
     * @param other other
     * @return add big complex result
     */
    public NBigComplex addBigComplex(NBigComplex other) {
        return new NBigComplexImpl(real.add(other.realValue()), imag.add(other.imagValue()));
    }


    /**
     * Negate big complex.
     *
     * @return negate big complex result
     */
    public NBigComplex negateBigComplex() {
        return new NBigComplexImpl(real.negate(), imag.negate());
    }

    /**
     * Subtract big complex.
     *
     * @param other other
     * @return subtract big complex result
     */
    public NBigComplex subtractBigComplex(NBigComplex other) {
        return new NBigComplexImpl(real.subtract(other.realValue()), imag.subtract(other.imagValue()));
    }


    /**
     * Multiply big complex.
     *
     * @param z2 z2
     * @param mc mc
     * @return multiply big complex result
     */
    public NBigComplex multiplyBigComplex(NBigComplex z2, MathContext mc) {
        BigDecimal a = this.real;
        BigDecimal b = this.imag;
        BigDecimal c = z2.realValue();
        BigDecimal d = z2.imagValue();
        mc= NNumberUtils.contextMathContext(mc);
        BigDecimal p1 = a.multiply(c, mc);
        BigDecimal p2 = b.multiply(d, mc);
        BigDecimal p3 = (a.add(b, mc)).multiply(c.add(d, mc), mc);

        BigDecimal real = p1.subtract(p2, mc);
        BigDecimal imag = p3.subtract(p1, mc).subtract(p2, mc);

        return new NBigComplexImpl(real, imag);
    }

    /**
     * Divide big complex.
     *
     * @param other other
     * @param mc mc
     * @return divide big complex result
     */
    public NBigComplex divideBigComplex(NBigComplex other, MathContext mc) {
        mc= NNumberUtils.contextMathContext(mc);
        BigDecimal c = other.realValue();
        BigDecimal d = other.imagValue();
        BigDecimal denominator = c.multiply(c, mc).add(d.multiply(d, mc), mc);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            /**
             * Arithmetic exception.
             *
             * @param number." number."
             * @return arithmetic exception result
             */
            throw new ArithmeticException("Division by zero complex number.");
        }

        BigDecimal real = this.real.multiply(c, mc).add(this.imag.multiply(d, mc), mc)
                .divide(denominator, mc);
        BigDecimal imag = this.imag.multiply(c, mc).subtract(this.real.multiply(d, mc), mc)
                .divide(denominator, mc);
        return new NBigComplexImpl(real, imag);
    }

    /**
     * Inv big complex.
     *
     * @param mc mc
     * @return inv big complex result
     */
    public NBigComplex invBigComplex(MathContext mc) {
        mc= NNumberUtils.contextMathContext(mc);
        BigDecimal denominator = this.real.multiply(this.real, mc).add(this.imag.multiply(this.imag, mc), mc);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            /**
             * Arithmetic exception.
             *
             * @param undefined." undefined."
             * @return arithmetic exception result
             */
            throw new ArithmeticException("Inverse of zero complex number is undefined.");
        }
        return new NBigComplexImpl(
                this.real.divide(denominator, mc),
                this.imag.negate().divide(denominator, mc)
        );
    }
}
