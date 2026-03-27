package net.thevpc.nuts.math;

import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;

public class NFloatComplexImpl extends Number implements NFloatComplex {
    public static final NFloatComplex ZERO = new NFloatComplexImpl(0, 0);
    public static final NFloatComplex ONE = new NFloatComplexImpl(1, 0);
    public static final NFloatComplex I = new NFloatComplexImpl(0, 1);
    private final float real;
    private final float imag;

    public static NFloatComplex of(String any) {
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
            any = any.trim();
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NFloatComplexImpl(Float.parseFloat(c[0]), Float.parseFloat(c[1])));
        } catch (Exception e) {
            return NOptional.ofNamedError("complex : " + any);
        }
    }

    public static NFloatComplex of(float x, float y) {
        return new NFloatComplexImpl(x, y);
    }

    public static NFloatComplex ofPolar(float r, float theta) {
        return new NFloatComplexImpl((float) (r * Math.cos(theta)), (float) (r * Math.sin(theta)));
    }

    public NFloatComplexImpl(float real, float imag) {
        this.real = real;
        this.imag = imag;
    }

    @Override
    public Number numberValue() {
        return this;
    }

    public float realValue() {
        return real;
    }

    public float imagValue() {
        return imag;
    }

    public float absFloat() {
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

    public NFloatComplex addFloatComplex(NFloatComplex other) {
        return new NFloatComplexImpl(real + other.realValue(), imag + other.imagValue());
    }

    public NFloatComplex negateFloatComplex() {
        return new NFloatComplexImpl(-real, -imag);
    }

    public NFloatComplex subtractFloatComplex(NFloatComplex other) {
        return new NFloatComplexImpl(real - other.realValue(), imag - other.imagValue());
    }

    public NFloatComplex multiplyFloatComplex(NFloatComplex z2) {
        float oreal = z2.realValue();
        float oimag = z2.imagValue();
        float real = this.real * oreal - this.imag * oimag;
        float imag = this.real * oimag + this.imag * oreal;
        return new NFloatComplexImpl(real, imag);
    }

    public NFloatComplex divideFloatComplex(NFloatComplex other) {
        float c = other.realValue();
        float d = other.imagValue();
        float denominator = c * c + d * d;
        return new NFloatComplexImpl(
                (this.real * c + this.imag * d) / denominator,
                (this.imag * c - this.real * d) / denominator
        );
    }

    public NFloatComplex invFloatComplex() {
        float denominator = this.real * this.real + this.imag * this.imag;
        return new NFloatComplexImpl(this.real / denominator, -this.imag / denominator);
    }
}
