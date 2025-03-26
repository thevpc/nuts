package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NAssert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class NBigComplex extends Number implements Serializable {
    private static final BigDecimal BIG_DECIMAL_MINUS_ONE = new BigDecimal("-1");
    public static final NBigComplex ZERO = new NBigComplex(BigDecimal.ZERO, BigDecimal.ZERO);
    public static final NBigComplex ONE = new NBigComplex(BigDecimal.ONE, BigDecimal.ZERO);
    public static final NBigComplex I = new NBigComplex(BigDecimal.ZERO, BigDecimal.ONE);
    private BigDecimal real;
    private BigDecimal imag;

    public NBigComplex(BigDecimal real, BigDecimal imag) {
        this.real = NAssert.requireNonNull(real, "real");
        this.imag = NAssert.requireNonNull(imag, "imag");
    }

    public BigDecimal real() {
        return real;
    }

    public BigDecimal imag() {
        return imag;
    }

    @Override
    public double doubleValue() {
        return real().doubleValue();
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
        NBigComplex ndComplex = (NBigComplex) o;
        return real.compareTo(ndComplex.real) == 0 && imag.compareTo(ndComplex.imag) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(real, imag);
    }

    @Override
    public String toString() {
        if (imag.equals(BigDecimal.ZERO)) {
            return realToString(real);
        } else if (real.equals(BigDecimal.ZERO)) {
            return imagToString(imag);
        } else {
            if (imag.compareTo(BigDecimal.ZERO) < 0) {
                return realToString(real) + imagToString(imag);
            }
            return realToString(real) + "+" + imagToString(imag);
        }
    }

    protected String realToString(BigDecimal d) {
        return String.valueOf(d);
    }

    protected String imagToString(BigDecimal d) {
        if (d.equals(BigDecimal.ONE)) {
            return "î";
        }
        if (d.equals(BIG_DECIMAL_MINUS_ONE)) {
            return "-î";
        }
        return d + "î";
    }
}
