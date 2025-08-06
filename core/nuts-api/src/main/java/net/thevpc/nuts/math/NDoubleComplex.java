package net.thevpc.nuts.math;

import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.util.Objects;

public class NDoubleComplex extends Number implements Serializable, Comparable<NDoubleComplex> {
    public static final NDoubleComplex ZERO = new NDoubleComplex(0, 0);
    public static final NDoubleComplex ONE = new NDoubleComplex(1, 0);
    public static final NDoubleComplex I = new NDoubleComplex(0, 1);
    private double real;
    private double imag;

    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    public NOptional<NDoubleComplex> of(String any) {
        try {
            if (NBlankable.isBlank(any)) {
                return NOptional.ofNamedEmpty("complex");
            }
            any = any.trim();
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NDoubleComplex(Double.parseDouble(c[0]), Double.parseDouble(c[1])));
        } catch (Exception e) {
            return NOptional.ofNamedError("complex : " + any);
        }
    }

    public NDoubleComplex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public double real() {
        return real;
    }

    public double imag() {
        return imag;
    }

    public double abs() {
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
        NDoubleComplex ndComplex = (NDoubleComplex) o;
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

    @Override
    public int compareTo(NDoubleComplex other) {
        boolean thisNaN = Double.isNaN(this.real) || Double.isNaN(this.imag);
        boolean otherNaN = Double.isNaN(other.real) || Double.isNaN(other.imag);
        if (thisNaN && otherNaN) return 0;
        if (thisNaN) return 1;
        if (otherNaN) return -1;

        double mag1 = this.real * this.real + this.imag * this.imag;
        double mag2 = other.real * other.real + other.imag * other.imag;
        if (mag1 < mag2) return -1;
        if (mag1 > mag2) return 1;

        int cmpReal = Double.compare(this.real, other.real);
        if (cmpReal != 0) return cmpReal;

        return Double.compare(this.imag, other.imag);
    }

    public NDoubleComplex add(NDoubleComplex other) {
        return new NDoubleComplex(real + other.real, imag + other.imag);
    }

    public NDoubleComplex negate() {
        return new NDoubleComplex(-real, -imag);
    }

    public NDoubleComplex subtract(NDoubleComplex other) {
        return new NDoubleComplex(real - other.real, imag - other.imag);
    }

    public NDoubleComplex multiply(NDoubleComplex z2) {
        double real = this.real * z2.real - this.imag * z2.imag;
        double imag = this.real * z2.imag + this.imag * z2.real;
        return new NDoubleComplex(real, imag);
    }

    public NDoubleComplex divide(NDoubleComplex other) {
        double c = other.real;
        double d = other.imag;
        double denominator = c * c + d * d;
        return new NDoubleComplex(
                (this.real * c + this.imag * d) / denominator,
                (this.imag * c - this.real * d) / denominator
        );
    }

    public NDoubleComplex inv() {
        double denominator = this.real * this.real + this.imag * this.imag;
        return new NDoubleComplex(this.real / denominator, -this.imag / denominator);
    }
}
