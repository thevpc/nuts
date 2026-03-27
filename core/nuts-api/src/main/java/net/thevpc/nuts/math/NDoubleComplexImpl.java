package net.thevpc.nuts.math;

import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;

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
            any = any.trim();
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NDoubleComplexImpl(Double.parseDouble(c[0]), Double.parseDouble(c[1])));
        } catch (Exception e) {
            return NOptional.ofNamedError("complex : " + any);
        }
    }

    public static NDoubleComplex of(double x, double y) {
        return new NDoubleComplexImpl(x, y);
    }

    public static NDoubleComplex ofPolar(double r, double theta) {
        return new NDoubleComplexImpl(r * Math.cos(theta), r * Math.sin(theta));
    }

    public NDoubleComplexImpl(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    @Override
    public Number numberValue() {
        return this;
    }

    public double realValue() {
        return real;
    }

    public double imagValue() {
        return imag;
    }

    public double absDouble() {
        return Math.sqrt(real * real + imag * imag);
    }

    @Override
    public double doubleValue() {
        return real;
    }

    @Override
    public int intValue() {
        return (int) doubleValue();
    }

    @Override
    public long longValue() {
        return (long) doubleValue();
    }

    @Override
    public float floatValue() {
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
            return realToString(real);
        } else if (real == 0) {
            return imagToString(imag);
        } else {
            if (imag < 0) {
                return realToString(real) + imagToString(imag);
            }
            return realToString(real) + "+" + imagToString(imag);
        }
    }

    protected String realToString(double d) {
        return String.valueOf(d);
    }

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

    public NDoubleComplex addDoubleComplex(NDoubleComplex other) {
        return new NDoubleComplexImpl(real + other.realValue(), imag + other.imagValue());
    }

    public NDoubleComplex negateDoubleComplex() {
        return new NDoubleComplexImpl(-real, -imag);
    }

    public NDoubleComplex subtractDoubleComplex(NDoubleComplex other) {
        return new NDoubleComplexImpl(real - other.realValue(), imag - other.imagValue());
    }

    public NDoubleComplex multiplyDoubleComplex(NDoubleComplex z2) {
        double oreal = z2.realValue();
        double oimag = z2.imagValue();
        double real = this.real * oreal - this.imag * oimag;
        double imag = this.real * oimag + this.imag * oreal;
        return new NDoubleComplexImpl(real, imag);
    }

    public NDoubleComplex divideDoubleComplex(NDoubleComplex other) {
        double c = other.realValue();
        double d = other.imagValue();
        double denominator = c * c + d * d;
        return new NDoubleComplexImpl(
                (this.real * c + this.imag * d) / denominator,
                (this.imag * c - this.real * d) / denominator
        );
    }

    public NDoubleComplex invDoubleComplex() {
        double denominator = this.real * this.real + this.imag * this.imag;
        return new NDoubleComplexImpl(this.real / denominator, -this.imag / denominator);
    }
}
