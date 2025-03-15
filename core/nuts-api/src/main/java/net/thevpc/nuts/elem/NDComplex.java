package net.thevpc.nuts.elem;

import java.io.Serializable;
import java.util.Objects;

public class NDComplex extends Number implements Serializable {
    public static final NDComplex ZERO = new NDComplex(0, 0);
    public static final NDComplex ONE = new NDComplex(1, 0);
    public static final NDComplex I = new NDComplex(0, 1);
    private double real;
    private double imag;

    public NDComplex(double real, double imag) {
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
        return Math.sqrt(real*real + imag*imag);
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
        NDComplex ndComplex = (NDComplex) o;
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
}
