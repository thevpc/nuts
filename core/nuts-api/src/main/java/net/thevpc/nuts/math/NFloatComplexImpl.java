package net.thevpc.nuts.math;

import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;

/**
 * NFloatComplexImpl class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NFloatComplexImpl extends Number implements NFloatComplex {
    public static final NFloatComplex ZERO = new NFloatComplexImpl(0, 0);
    public static final NFloatComplex ONE = new NFloatComplexImpl(1, 0);
    public static final NFloatComplex I = new NFloatComplexImpl(0, 1);
    private final float real;
    private final float imag;

    /**
     * Creates a new instance of of.
     *
     * @param any any
     * @return of result
     */
    public static NFloatComplex of(String any) {
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
    public static NOptional<NFloatComplex> parse(String any) {
        try {
            if (NBlankable.isBlank(any)) {
                return NOptional.ofNamedEmpty("complex");
            }
            any = NStringUtils.strip(any);
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NFloatComplexImpl(Float.parseFloat(c[0]), Float.parseFloat(c[1])));
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
    public static NFloatComplex of(float x, float y) {
        return new NFloatComplexImpl(x, y);
    }

    /**
     * Creates a new instance of of polar.
     *
     * @param r r
     * @param theta theta
     * @return of polar result
     */
    public static NFloatComplex ofPolar(float r, float theta) {
        return new NFloatComplexImpl((float) (r * Math.cos(theta)), (float) (r * Math.sin(theta)));
    }

    /**
     * N float complex impl.
     *
     * @param real real
     * @param imag imag
     * @return n float complex impl result
     */
    public NFloatComplexImpl(float real, float imag) {
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
    public float realValue() {
        return real;
    }

    /**
     * Imag value.
     *
     * @return imag value result
     */
    public float imagValue() {
        return imag;
    }

    /**
     * Abs float.
     *
     * @return abs float result
     */
    public float absFloat() {
      /**
       * Return.
       *
       * @param imag imag
       */
        return (float) Math.sqrt(real * real + imag * imag);
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
        NFloatComplexImpl ndComplex = (NFloatComplexImpl) o;
        return Float.compare(real, ndComplex.real) == 0 && Float.compare(imag, ndComplex.imag) == 0;
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
    protected String realToString(float d) {
        return String.valueOf(d);
    }

    /**
     * Imag to string.
     *
     * @param d d
     * @return imag to string result
     */
    protected String imagToString(float d) {
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
     * Compare to float complex.
     *
     * @param other other
     * @return compare to float complex result
     */
    public int compareToFloatComplex(NFloatComplexImpl other) {
        boolean thisNaN = Float.isNaN(this.real) || Float.isNaN(this.imag);
        boolean otherNaN = Float.isNaN(other.real) || Float.isNaN(other.imag);
        if (thisNaN && otherNaN) return 0;
        if (thisNaN) return 1;
        if (otherNaN) return -1;

        float mag1 = this.real * this.real + this.imag * this.imag;
        float mag2 = other.real * other.real + other.imag * other.imag;
        if (mag1 < mag2) return -1;
        if (mag1 > mag2) return 1;

        int cmpReal = Float.compare(this.real, other.real);
        if (cmpReal != 0) return cmpReal;

        return Float.compare(this.imag, other.imag);
    }

    /**
     * Adds the specified float complex.
     *
     * @param other other
     * @return add float complex result
     */
    public NFloatComplex addFloatComplex(NFloatComplex other) {
        return new NFloatComplexImpl(real + other.realValue(), imag + other.imagValue());
    }

    /**
     * Negate float complex.
     *
     * @return negate float complex result
     */
    public NFloatComplex negateFloatComplex() {
        return new NFloatComplexImpl(-real, -imag);
    }

    /**
     * Subtract float complex.
     *
     * @param other other
     * @return subtract float complex result
     */
    public NFloatComplex subtractFloatComplex(NFloatComplex other) {
        return new NFloatComplexImpl(real - other.realValue(), imag - other.imagValue());
    }

    /**
     * Multiply float complex.
     *
     * @param z2 z2
     * @return multiply float complex result
     */
    public NFloatComplex multiplyFloatComplex(NFloatComplex z2) {
        float oreal = z2.realValue();
        float oimag = z2.imagValue();
        float real = this.real * oreal - this.imag * oimag;
        float imag = this.real * oimag + this.imag * oreal;
        return new NFloatComplexImpl(real, imag);
    }

    /**
     * Divide float complex.
     *
     * @param other other
     * @return divide float complex result
     */
    public NFloatComplex divideFloatComplex(NFloatComplex other) {
        float c = other.realValue();
        float d = other.imagValue();
        float denominator = c * c + d * d;
        return new NFloatComplexImpl(
                (this.real * c + this.imag * d) / denominator,
                (this.imag * c - this.real * d) / denominator
        );
    }

    /**
     * Inv float complex.
     *
     * @return inv float complex result
     */
    public NFloatComplex invFloatComplex() {
        float denominator = this.real * this.real + this.imag * this.imag;
        return new NFloatComplexImpl(this.real / denominator, -this.imag / denominator);
    }
}
