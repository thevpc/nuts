package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public class NBigComplex extends Number implements Serializable, Comparable<NBigComplex> {
    private static final BigDecimal BIG_DECIMAL_MINUS_ONE = new BigDecimal("-1");
    public static final NBigComplex ZERO = new NBigComplex(BigDecimal.ZERO, BigDecimal.ZERO);
    public static final NBigComplex ONE = new NBigComplex(BigDecimal.ONE, BigDecimal.ZERO);
    public static final NBigComplex I = new NBigComplex(BigDecimal.ZERO, BigDecimal.ONE);
    private BigDecimal real;
    private BigDecimal imag;

    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    public NOptional<NBigComplex> of(String any) {
        try {
            if (NBlankable.isBlank(any)) {
                return NOptional.ofNamedEmpty("complex");
            }
            any = any.trim();
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NBigComplex(new BigDecimal(c[0]), new BigDecimal(c[1])));
        } catch (Exception e) {
            return NOptional.ofNamedError("complex : " + any);
        }
    }

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

    @Override
    public int compareTo(NBigComplex other) {
        BigDecimal mag1 = real.multiply(real).add(imag.multiply(imag));
        BigDecimal mag2 = other.real.multiply(other.real).add(other.imag.multiply(other.imag));
        int cmp = mag1.compareTo(mag2);
        if (cmp != 0) return cmp;

        cmp = real.compareTo(other.real);
        if (cmp != 0) return cmp;

        return imag.compareTo(other.imag);
    }

    public NBigComplex add(NBigComplex other) {
        return new NBigComplex(real.add(other.real), imag.add(other.imag));
    }


    public NBigComplex negate() {
        return new NBigComplex(real.negate(), imag.negate());
    }

    public NBigComplex subtract(NBigComplex other) {
        return new NBigComplex(real.subtract(other.real), imag.subtract(other.imag));
    }


    public NBigComplex multiply(NBigComplex z2, MathContext mc) {
        BigDecimal a = this.real;
        BigDecimal b = this.imag;
        BigDecimal c = z2.real;
        BigDecimal d = z2.imag;

        BigDecimal p1 = a.multiply(c, mc);
        BigDecimal p2 = b.multiply(d, mc);
        BigDecimal p3 = (a.add(b, mc)).multiply(c.add(d, mc), mc);

        BigDecimal real = p1.subtract(p2, mc);
        BigDecimal imag = p3.subtract(p1, mc).subtract(p2, mc);

        return new NBigComplex(real, imag);
    }

    public NBigComplex divide(NBigComplex other, MathContext mc) {
        BigDecimal c = other.real;
        BigDecimal d = other.imag;
        BigDecimal denominator = c.multiply(c, mc).add(d.multiply(d, mc), mc);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero complex number.");
        }

        BigDecimal real = this.real.multiply(c, mc).add(this.imag.multiply(d, mc), mc)
                .divide(denominator, mc);
        BigDecimal imag = this.imag.multiply(c, mc).subtract(this.real.multiply(d, mc), mc)
                .divide(denominator, mc);
        return new NBigComplex(real, imag);
    }

    public NBigComplex inv(MathContext mc) {
        BigDecimal denominator = this.real.multiply(this.real, mc).add(this.imag.multiply(this.imag, mc), mc);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Inverse of zero complex number is undefined.");
        }
        return new NBigComplex(
                this.real.divide(denominator, mc),
                this.imag.negate().divide(denominator, mc)
        );
    }
}
