package net.thevpc.nuts.math;

import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;

/**
 * NDoubleComplexImpl class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NDoubleComplexImpl extends Number implements NDoubleComplex {
    public static final NDoubleComplexImpl ZERO = new NDoubleComplexImpl(0, 0);
    public static final NDoubleComplexImpl ONE = new NDoubleComplexImpl(1, 0);
    public static final NDoubleComplexImpl I = new NDoubleComplexImpl(0, 1);
    private final double real;
    private final double imag;

    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    public static NDoubleComplex of(String any) {
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
    public static NOptional<NDoubleComplex> parse(String any) {
        try {
            if (NBlankable.isBlank(any)) {
                return NOptional.ofNamedEmpty("complex");
            }
            any = NStringUtils.strip(any);
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NDoubleComplexImpl(Double.parseDouble(c[0]), Double.parseDouble(c[1])));
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
    public static NDoubleComplex of(double x, double y) {
        return new NDoubleComplexImpl(x, y);
    }

    /**
     * Creates a new instance of of polar.
     *
     * @param r r
     * @param theta theta
     * @return of polar result
     */
    public static NDoubleComplex ofPolar(double r, double theta) {
        return new NDoubleComplexImpl(r * Math.cos(theta), r * Math.sin(theta));
    }

    /**
     * N double complex impl.
     *
     * @param real real
     * @param imag imag
     * @return n double complex impl result
     */
    public NDoubleComplexImpl(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    @Override
    public boolean isReal() {
        return imag == 0;
    }

    @Override
    public boolean isImaginary() {
        return real == 0 && imag !=0;
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
    public double realValue() {
        return real;
    }

    /**
     * Imag value.
     *
     * @return imag value result
     */
    public double imagValue() {
        return imag;
    }

    /**
     * Abs double.
     *
     * @return abs double result
     */
    public double absDouble() {
        return Math.sqrt(real * real + imag * imag);
    }

    @Override
    public double doubleValue() {
        return real;
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
        NDoubleComplexImpl ndComplex = (NDoubleComplexImpl) o;
        return Double.compare(real, ndComplex.real) == 0 && Double.compare(imag, ndComplex.imag) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(real, imag);
    }

    @Override
    public String toString() {
        if (imag == 0) {
            /**
             * Real to string.
             *
             * @param real real
             * @return real to string result
             */
            return realToString(real);
        } else if (real == 0) {
            /**
             * Imag to string.
             *
             * @param imag imag
             * @return imag to string result
             */
            return imagToString(imag);
        } else {
            if (imag < 0) {
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
    protected String realToString(double d) {
        return String.valueOf(d);
    }

    /**
     * Imag to string.
     *
     * @param d d
     * @return imag to string result
     */
    protected String imagToString(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            return d + "*î";
        }
        if (d == 1) {
            return "î";
        }
        if (d == -1) {
            return "-î";
        }
        return d + "î";
    }

    /**
     * Compare to double complex.
     *
     * @param other other
     * @return compare to double complex result
     */
    public int compareToDoubleComplex(NDoubleComplex other) {
        boolean thisNaN = Double.isNaN(this.real) || Double.isNaN(this.imag);
        double oreal = other.realValue();
        double oimag = other.imagValue();
        boolean otherNaN = Double.isNaN(oreal) || Double.isNaN(oimag);
        if (thisNaN && otherNaN) return 0;
        if (thisNaN) return 1;
        if (otherNaN) return -1;

        double mag1 = this.real * this.real + this.imag * this.imag;
        double mag2 = oreal * oreal + oimag * oimag;
        if (mag1 < mag2) return -1;
        if (mag1 > mag2) return 1;

        int cmpReal = Double.compare(this.real, oreal);
        if (cmpReal != 0) return cmpReal;

        return Double.compare(this.imag, oimag);
    }

    /**
     * Adds the specified double complex.
     *
     * @param other other
     * @return add double complex result
     */
    public NDoubleComplex addDoubleComplex(NDoubleComplex other) {
        return new NDoubleComplexImpl(real + other.realValue(), imag + other.imagValue());
    }

    /**
     * Negate double complex.
     *
     * @return negate double complex result
     */
    public NDoubleComplex negateDoubleComplex() {
        return new NDoubleComplexImpl(-real, -imag);
    }

    /**
     * Subtract double complex.
     *
     * @param other other
     * @return subtract double complex result
     */
    public NDoubleComplex subtractDoubleComplex(NDoubleComplex other) {
        return new NDoubleComplexImpl(real - other.realValue(), imag - other.imagValue());
    }

    /**
     * Multiply double complex.
     *
     * @param z2 z2
     * @return multiply double complex result
     */
    public NDoubleComplex multiplyDoubleComplex(NDoubleComplex z2) {
        double oreal = z2.realValue();
        double oimag = z2.imagValue();
        double real = this.real * oreal - this.imag * oimag;
        double imag = this.real * oimag + this.imag * oreal;
        return new NDoubleComplexImpl(real, imag);
    }

    /**
     * Divide double complex.
     *
     * @param other other
     * @return divide double complex result
     */
    public NDoubleComplex divideDoubleComplex(NDoubleComplex other) {
        double c = other.realValue();
        double d = other.imagValue();
        double denominator = c * c + d * d;
        return new NDoubleComplexImpl(
                (this.real * c + this.imag * d) / denominator,
                (this.imag * c - this.real * d) / denominator
        );
    }

    /**
     * Inv double complex.
     *
     * @return inv double complex result
     */
    public NDoubleComplex invDoubleComplex() {
        double denominator = this.real * this.real + this.imag * this.imag;
        return new NDoubleComplexImpl(this.real / denominator, -this.imag / denominator);
    }
}
