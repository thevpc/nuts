package net.thevpc.nuts.elem;

import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.util.Objects;

public class NFloatComplex extends Number implements Serializable {
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
}
