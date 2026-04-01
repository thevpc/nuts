package net.thevpc.nuts.math;

import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NNumberUtils;
import net.thevpc.nuts.util.NOptional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public class NBigComplexImpl extends Number implements NBigComplex{
    private static final BigDecimal BIG_DECIMAL_MINUS_ONE = new BigDecimal("-1");
    public static final NBigComplex ZERO = new NBigComplexImpl(BigDecimal.ZERO, BigDecimal.ZERO);
    public static final NBigComplex ONE = new NBigComplexImpl(BigDecimal.ONE, BigDecimal.ZERO);
    public static final NBigComplex I = new NBigComplexImpl(BigDecimal.ZERO, BigDecimal.ONE);
    private BigDecimal real;
    private BigDecimal imag;

    public static NBigComplex of(String any) {
        return parse(any).get();
    }
    /**
     * @param any string
     * @return optional of complex
     * @since 0.8.6
     */
    public static NOptional<NBigComplex> parse(String any) {
        try {
            if (NBlankable.isBlank(any)) {
                return NOptional.ofNamedEmpty("complex");
            }
            any = any.trim();
            String[] c = NReservedUtils.parseComplexStrings(any);
            return NOptional.of(new NBigComplexImpl(new BigDecimal(c[0]), new BigDecimal(c[1])));
        } catch (Exception e) {
            return NOptional.ofNamedError("complex : " + any);
        }
    }

    public static NBigComplex of(BigDecimal x, BigDecimal y) {
        return new NBigComplexImpl(x, y);
    }

    public static NBigComplex ofPolar(BigDecimal r, double theta) {
        return new NBigComplexImpl(r.multiply(new BigDecimal(Math.cos(theta))), r.multiply(new BigDecimal(Math.sin(theta))));
    }

    public NBigComplexImpl(BigDecimal real, BigDecimal imag) {
        this.real = NAssert.requireNamedNonNull(real, "real");
        this.imag = NAssert.requireNamedNonNull(imag, "imag");
    }


    @Override
    public boolean isReal() {
        return imag.equals(BigDecimal.ZERO);
    }

    @Override
    public boolean isImaginary() {
        return real.equals(BigDecimal.ZERO) && !imag.equals(BigDecimal.ZERO);
    }


    @Override
    public Number numberValue() {
        return this;
    }

    public BigDecimal realValue() {
        return real;
    }

    public BigDecimal imagValue() {
        return imag;
    }

    @Override
    public double doubleValue() {
        return realValue().doubleValue();
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
        NBigComplexImpl ndComplex = (NBigComplexImpl) o;
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

    public int compareTo(NBigComplex other) {
        BigDecimal oreal = other.realValue();
        BigDecimal oimag = other.imagValue();

        BigDecimal mag1 = real.multiply(real).add(imag.multiply(imag));
        BigDecimal mag2 = oreal.multiply(oreal).add(oimag.multiply(oimag));
        int cmp = mag1.compareTo(mag2);
        if (cmp != 0) return cmp;

        cmp = real.compareTo(oreal);
        if (cmp != 0) return cmp;

        return imag.compareTo(oimag);
    }

    public NBigComplex addBigComplex(NBigComplex other) {
        return new NBigComplexImpl(real.add(other.realValue()), imag.add(other.imagValue()));
    }


    public NBigComplex negateBigComplex() {
        return new NBigComplexImpl(real.negate(), imag.negate());
    }

    public NBigComplex subtractBigComplex(NBigComplex other) {
        return new NBigComplexImpl(real.subtract(other.realValue()), imag.subtract(other.imagValue()));
    }


    public NBigComplex multiplyBigComplex(NBigComplex z2, MathContext mc) {
        BigDecimal a = this.real;
        BigDecimal b = this.imag;
        BigDecimal c = z2.realValue();
        BigDecimal d = z2.imagValue();
        mc= NNumberUtils.getContextMathContext(mc);
        BigDecimal p1 = a.multiply(c, mc);
        BigDecimal p2 = b.multiply(d, mc);
        BigDecimal p3 = (a.add(b, mc)).multiply(c.add(d, mc), mc);

        BigDecimal real = p1.subtract(p2, mc);
        BigDecimal imag = p3.subtract(p1, mc).subtract(p2, mc);

        return new NBigComplexImpl(real, imag);
    }

    public NBigComplex divideBigComplex(NBigComplex other, MathContext mc) {
        mc= NNumberUtils.getContextMathContext(mc);
        BigDecimal c = other.realValue();
        BigDecimal d = other.imagValue();
        BigDecimal denominator = c.multiply(c, mc).add(d.multiply(d, mc), mc);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero complex number.");
        }

        BigDecimal real = this.real.multiply(c, mc).add(this.imag.multiply(d, mc), mc)
                .divide(denominator, mc);
        BigDecimal imag = this.imag.multiply(c, mc).subtract(this.real.multiply(d, mc), mc)
                .divide(denominator, mc);
        return new NBigComplexImpl(real, imag);
    }

    public NBigComplex invBigComplex(MathContext mc) {
        mc= NNumberUtils.getContextMathContext(mc);
        BigDecimal denominator = this.real.multiply(this.real, mc).add(this.imag.multiply(this.imag, mc), mc);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Inverse of zero complex number is undefined.");
        }
        return new NBigComplexImpl(
                this.real.divide(denominator, mc),
                this.imag.negate().divide(denominator, mc)
        );
    }
}
