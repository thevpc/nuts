package net.thevpc.nuts.math;

import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.util.Objects;

public class NFloatComplex extends Number implements Serializable, Comparable<NFloatComplex> {
    public static final NFloatComplex ZERO = new NFloatComplex(0, 0);
    public static final NFloatComplex ONE = new NFloatComplex(1, 0);
    public static final NFloatComplex I = new NFloatComplex(0, 1);
    private float real;
    private float imag;

    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    public NOptional<NFloatComplex> of(String any) {
        try {
            if (NBlankable.isBlank(any)) {
                return NOptional.ofNamedEmpty("complex");
            }
            any = any.trim();
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NFloatComplex(Float.parseFloat(c[0]), Float.parseFloat(c[1])));
        } catch (Exception e) {
            return NOptional.ofNamedError("complex : " + any);
        }
    }

    public NFloatComplex(float real, float imag) {
        this.real = real;
        this.imag = imag;
    }

    public float real() {
        return real;
    }

    public float imag() {
        return imag;
    }

    public float abs() {
        return (float) Math.sqrt(real * real + imag * imag);
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
        NFloatComplex ndComplex = (NFloatComplex) o;
        return Float.compare(real, ndComplex.real) == 0 && Float.compare(imag, ndComplex.imag) == 0;
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

    protected String realToString(float d) {
        return String.valueOf(d);
    }

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

    @Override
    public int compareTo(NFloatComplex other) {
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

    public NFloatComplex add(NFloatComplex other) {
        return new NFloatComplex(real + other.real, imag + other.imag);
    }

    public NFloatComplex negate() {
        return new NFloatComplex(-real, -imag);
    }

    public NFloatComplex subtract(NFloatComplex other) {
        return new NFloatComplex(real - other.real, imag - other.imag);
    }

    public NFloatComplex multiply(NFloatComplex z2) {
        float real = this.real * z2.real - this.imag * z2.imag;
        float imag = this.real * z2.imag + this.imag * z2.real;
        return new NFloatComplex(real, imag);
    }

    public NFloatComplex divide(NFloatComplex other) {
        float c = other.real;
        float d = other.imag;
        float denominator = c * c + d * d;
        return new NFloatComplex(
                (this.real * c + this.imag * d) / denominator,
                (this.imag * c - this.real * d) / denominator
        );
    }
    public NFloatComplex inv() {
        float denominator = this.real * this.real + this.imag * this.imag;
        return new NFloatComplex(this.real / denominator, -this.imag / denominator);
    }
}
