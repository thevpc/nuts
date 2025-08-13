package net.thevpc.nuts.util;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class NNumberUtils {
    public static ThreadLocal<MathContext> MATH_CONTEXT = new ThreadLocal<>();

    public static <T> T callWithMathContext(MathContext mc, NCallable<T> c) {
        MathContext o = MATH_CONTEXT.get();
        MATH_CONTEXT.set(mc);
        try {
            return c.call();
        } finally {
            MATH_CONTEXT.set(o);
        }
    }

    public static MathContext getContextMathContext(MathContext user) {
        if (user != null) {
            return user;
        }
        return getContextMathContext();
    }

    public static MathContext getContextMathContext() {
        MathContext a = MATH_CONTEXT.get();
        return a == null ? MathContext.DECIMAL128 : a;
    }

    public static void runWithMathContext(MathContext mc, Runnable c) {
        MathContext o = MATH_CONTEXT.get();
        MATH_CONTEXT.set(mc);
        try {
            c.run();
        } finally {
            MATH_CONTEXT.set(o);
        }
    }

    public static NElementType elementNumberType(Class<? extends Number> aa, NElementType def) {
        if (aa == null) {
            return null;
        }
        switch (aa.getName()) {
            case "java.math.BigDecimal":
                return NElementType.BIG_DECIMAL;
            case "java.math.BigInteger":
                return NElementType.BIG_INT;
            case "net.thevpc.nuts.math.NBigComplex":
                return NElementType.BIG_COMPLEX;
            case "net.thevpc.nuts.math.NDoubleComplex":
                return NElementType.DOUBLE_COMPLEX;
            case "net.thevpc.nuts.math.NFloatComplex":
                return NElementType.FLOAT_COMPLEX;
            case "java.lang.Double":
                return NElementType.DOUBLE;
            case "java.lang.Float":
                return NElementType.FLOAT;
            case "java.lang.Long":
                return NElementType.LONG;
            case "java.lang.Integer":
                return NElementType.INT;
            case "java.lang.Short":
                return NElementType.SHORT;
            case "java.lang.Byte":
                return NElementType.BYTE;
        }
        if (NBigComplex.class.isAssignableFrom(aa)) {
            return NElementType.BIG_COMPLEX;
        }
        if (NDoubleComplex.class.isAssignableFrom(aa)) {
            return NElementType.DOUBLE_COMPLEX;
        }
        if (NFloatComplex.class.isAssignableFrom(aa)) {
            return NElementType.FLOAT_COMPLEX;
        }
        if (BigDecimal.class.isAssignableFrom(aa)) {
            return NElementType.BIG_DECIMAL;
        }
        if (BigInteger.class.isAssignableFrom(aa)) {
            return NElementType.BIG_INT;
        }
        return def;
    }

    public static double xorDoubles(double a, double b) {
        long aBits = Double.doubleToRawLongBits(a);
        long bBits = Double.doubleToRawLongBits(b);
        long xorBits = aBits ^ bBits;
        return Double.longBitsToDouble(xorBits);
    }

    public static float xorFloats(float a, float b) {
        int aBits = Float.floatToRawIntBits(a);
        int bBits = Float.floatToRawIntBits(b);
        int xorBits = aBits ^ bBits;
        return Float.intBitsToFloat(xorBits);
    }

    public static Number xorNumbers(Number a, Number b) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                return xorBigDecimals(NLiteral.of(a).asBigDecimal().get(), NLiteral.of(b).asBigDecimal().get());
            case BIG_INT:
                return NLiteral.of(a).asBigInt().get().xor(NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                return xorDoubles(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case FLOAT:
                return xorFloats(NLiteral.of(a).asFloat().get(), NLiteral.of(b).asFloat().get());
            case LONG:
                return NLiteral.of(a).asLong().get() ^ NLiteral.of(b).asLong().get();
            case INT:
                return NLiteral.of(a).asInt().get() ^ NLiteral.of(b).asInt().get();
            case SHORT:
                return NLiteral.of(a).asShort().get() ^ NLiteral.of(b).asShort().get();
            case BYTE:
                return NLiteral.of(a).asByte().get() ^ NLiteral.of(b).asByte().get();
        }
        throw new NIllegalArgumentException(NMsg.ofC("unable to xor numbers %s and %s", a, b));
    }

    public static Number orNumbers(Number a, Number b) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                return orBigDecimals(NLiteral.of(a).asBigDecimal().get(), NLiteral.of(b).asBigDecimal().get());
            case BIG_INT:
                return NLiteral.of(a).asBigInt().get().or(NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                return orDoubles(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case FLOAT:
                return orFloats(NLiteral.of(a).asFloat().get(), NLiteral.of(b).asFloat().get());
            case LONG:
                return NLiteral.of(a).asLong().get() | NLiteral.of(b).asLong().get();
            case INT:
                return NLiteral.of(a).asInt().get() | NLiteral.of(b).asInt().get();
            case SHORT:
                return NLiteral.of(a).asShort().get() | NLiteral.of(b).asShort().get();
            case BYTE:
                return NLiteral.of(a).asByte().get() | NLiteral.of(b).asByte().get();
        }
        throw new NIllegalArgumentException(NMsg.ofC("unable to or numbers %s and %s", a, b));
    }

    public static Number andNumbers(Number a, Number b) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                return andBigDecimals(NLiteral.of(a).asBigDecimal().get(), NLiteral.of(b).asBigDecimal().get());
            case BIG_INT:
                return NLiteral.of(a).asBigInt().get().or(NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                return andDoubles(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case FLOAT:
                return andFloats(NLiteral.of(a).asFloat().get(), NLiteral.of(b).asFloat().get());
            case LONG:
                return NLiteral.of(a).asLong().get() & NLiteral.of(b).asLong().get();
            case INT:
                return NLiteral.of(a).asInt().get() & NLiteral.of(b).asInt().get();
            case SHORT:
                return NLiteral.of(a).asShort().get() & NLiteral.of(b).asShort().get();
            case BYTE:
                return NLiteral.of(a).asByte().get() & NLiteral.of(b).asByte().get();
        }
        throw new NIllegalArgumentException(NMsg.ofC("unable to 'and' numbers %s and %s", a, b));
    }

    public static boolean eqNumbers(Number a, Number b) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                return NLiteral.of(a).asBigDecimal().get().equals(NLiteral.of(b).asBigDecimal().get());
            case BIG_INT:
                return NLiteral.of(a).asBigInt().get().equals(NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                return NLiteral.of(a).asDouble().get().equals(NLiteral.of(b).asDouble().get());
            case FLOAT:
                return NLiteral.of(a).asFloat().get().equals(NLiteral.of(b).asFloat().get());
            case LONG:
                return NLiteral.of(a).asLong().get().equals(NLiteral.of(b).asLong().get());
            case INT:
                return NLiteral.of(a).asInt().get().equals(NLiteral.of(b).asInt().get());
            case SHORT:
                return NLiteral.of(a).asShort().get().equals(NLiteral.of(b).asShort().get());
            case BYTE:
                return NLiteral.of(a).asByte().get().equals(NLiteral.of(b).asByte().get());
        }
        throw new NIllegalArgumentException(NMsg.ofC("unable to 'eq'' numbers %s and %s", a, b));
    }

    public static int compareNumbers(Number a, Number b) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                return NLiteral.of(a).asBigDecimal().get().compareTo(NLiteral.of(b).asBigDecimal().get());
            case BIG_INT:
                return NLiteral.of(a).asBigInt().get().compareTo(NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                return NLiteral.of(a).asDouble().get().compareTo(NLiteral.of(b).asDouble().get());
            case FLOAT:
                return NLiteral.of(a).asFloat().get().compareTo(NLiteral.of(b).asFloat().get());
            case LONG:
                return NLiteral.of(a).asLong().get().compareTo(NLiteral.of(b).asLong().get());
            case INT:
                return NLiteral.of(a).asInt().get().compareTo(NLiteral.of(b).asInt().get());
            case SHORT:
                return NLiteral.of(a).asShort().get().compareTo(NLiteral.of(b).asShort().get());
            case BYTE:
                return NLiteral.of(a).asByte().get().compareTo(NLiteral.of(b).asByte().get());
        }
        throw new NIllegalArgumentException(NMsg.ofC("unable to 'compare'' numbers %s and %s", a, b));
    }

    public static BigDecimal orBigDecimals(BigDecimal a, BigDecimal b) {
        return new BigDecimal(a.toBigInteger().or(b.toBigInteger()));
    }

    public static BigDecimal andBigDecimals(BigDecimal a, BigDecimal b) {
        return new BigDecimal(a.toBigInteger().and(b.toBigInteger()));
    }

    public static double orDoubles(double a, double b) {
        return Double.longBitsToDouble(
                Double.doubleToLongBits(a) | Double.doubleToLongBits(b)
        );
    }

    public static double andDoubles(double a, double b) {
        return Double.longBitsToDouble(
                Double.doubleToLongBits(a) & Double.doubleToLongBits(b)
        );
    }

    public static float orFloats(float a, float b) {
        return Float.intBitsToFloat(Float.floatToIntBits(a) | Float.floatToIntBits(b));
    }

    public static float andFloats(float a, float b) {
        return Float.intBitsToFloat(Float.floatToIntBits(a) & Float.floatToIntBits(b));
    }

    public static long powLongs(long base, long exponent) {
        if (exponent < 0) {
            throw new ArithmeticException("Negative exponent not supported for long");
        }
        long result = 1;
        long b = base;
        while (exponent > 0) {
            if ((exponent & 1) == 1) { // if current bit is 1
                result *= b;
            }
            b *= b;
            exponent >>= 1; // shift exponent to the right
        }
        return result;
    }

    public static BigInteger powBigInts(BigInteger base, BigInteger exponent) {
        if (exponent.signum() < 0) {
            throw new ArithmeticException("Negative exponent not supported for BigInteger");
        }
        try {
            // Quick path: exponent fits in int
            return base.pow(exponent.intValueExact());
        } catch (ArithmeticException e) {
            // Exponent too large to fit in int — fallback to exponentiation by squaring
            BigInteger result = BigInteger.ONE;
            BigInteger b = base;
            BigInteger exp = exponent;

            while (exp.signum() > 0) {
                if (exp.testBit(0)) { // odd exponent
                    result = result.multiply(b);
                }
                b = b.multiply(b);
                exp = exp.shiftRight(1); // divide exponent by 2
            }
            return result;
        }
    }

    public static int powInts(int base, int exponent) {
        if (exponent < 0) {
            throw new ArithmeticException("Negative exponent not supported for int");
        }
        int result = 1;
        int b = base;
        while (exponent > 0) {
            if ((exponent & 1) == 1) { // if current bit is 1
                result *= b;
            }
            b *= b;
            exponent >>= 1; // shift exponent to the right
        }
        return result;
    }

    public static Number powNumbers(Number a, Number b, MathContext mc) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                return powBigDecimals(NLiteral.of(a).asBigDecimal().get(), NLiteral.of(b).asBigDecimal().get(), mc);
            case BIG_INT:
                return powBigInts(NLiteral.of(a).asBigInt().get(), NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                return Math.pow(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case FLOAT:
                return (float) Math.pow(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case LONG:
                return powLongs(NLiteral.of(a).asLong().get(), NLiteral.of(b).asLong().get());
            case INT:
                return powInts(NLiteral.of(a).asInt().get(), NLiteral.of(b).asInt().get());
            case SHORT:
                return (short) powInts(NLiteral.of(a).asInt().get(), NLiteral.of(b).asInt().get());
            case BYTE:
                return (byte) powInts(NLiteral.of(a).asInt().get(), NLiteral.of(b).asInt().get());
        }
        throw new NIllegalArgumentException(NMsg.ofC("unable to xor numbers %s and %s", a, b));
    }

    public static BigDecimal xorBigDecimals(BigDecimal aa, BigDecimal bb) {
        if (aa.scale() <= 0 && bb.scale() <= 0) {
            BigInteger aInt = aa.toBigInteger();
            BigInteger bInt = bb.toBigInteger();
            return new BigDecimal(aInt.xor(bInt));
        }

        if (canUseDouble(aa) && canUseDouble(bb)) {
            double aDouble = aa.doubleValue();
            double bDouble = bb.doubleValue();
            long aBits = Double.doubleToRawLongBits(aDouble);
            long bBits = Double.doubleToRawLongBits(bDouble);
            long xorBits = aBits ^ bBits;
            double resultDouble = Double.longBitsToDouble(xorBits);
            return BigDecimal.valueOf(resultDouble);
        }

        // General fixed-point XOR for arbitrary BigDecimal
        int scale = Math.max(aa.scale(), bb.scale());
        BigDecimal aScaled = aa.setScale(scale, RoundingMode.DOWN);
        BigDecimal bScaled = bb.setScale(scale, RoundingMode.DOWN);

        BigInteger aInt = aScaled.movePointRight(scale).toBigInteger();
        BigInteger bInt = bScaled.movePointRight(scale).toBigInteger();

        BigInteger xorInt = aInt.xor(bInt);

        return new BigDecimal(xorInt).movePointLeft(scale);
    }

    public static BigDecimal powBigDecimals(BigDecimal x, BigDecimal y, MathContext mc) {
        mc= getContextMathContext(mc);

        if (y.scale() <= 0 && y.stripTrailingZeros().scale() <= 0) {
            return x.pow(y.intValueExact(), mc);
        }

        // Double fallback if safe
        if (canUseDouble(x) && canUseDouble(y)) {
            double xd = x.doubleValue();
            double yd = y.doubleValue();
            double res = Math.pow(xd, yd);
            return new BigDecimal(res, mc);
        }

        // Arbitrary-precision fallback: x^y = exp(y * ln(x))
        BigDecimal lnX = lnBigDecimals(x, mc); // requires BigDecimalMath library
        BigDecimal yLnX = y.multiply(lnX, mc);
        return exp(yLnX, mc);      // requires BigDecimalMath library
    }

    public static BigDecimal exp(BigDecimal x, MathContext mc) {
        BigDecimal sum = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        mc= getContextMathContext(mc);
        int n = 1;
        while (term.compareTo(BigDecimal.ZERO) != 0) {
            term = term.multiply(x, mc).divide(BigDecimal.valueOf(n), mc);
            sum = sum.add(term, mc);
            n++;
            if (term.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision())) < 0) break;
        }
        return sum;
    }

    public static BigDecimal lnBigDecimals(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) <= 0) throw new ArithmeticException("ln(x) undefined for x <= 0");
        mc= getContextMathContext(mc);

        BigDecimal two = BigDecimal.valueOf(2);
        BigDecimal y = x.subtract(BigDecimal.ONE).divide(x.add(BigDecimal.ONE), mc);
        BigDecimal y2 = y.multiply(y, mc);

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal term = y;
        int n = 0;
        while (term.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision())) > 0) {
            BigDecimal denom = BigDecimal.valueOf(2 * n + 1);
            sum = sum.add(term.divide(denom, mc), mc);
            term = term.multiply(y2, mc);
            n++;
        }
        return sum.multiply(two, mc);
    }

    public static boolean canUseDouble(BigDecimal bd) {
        if (bd.abs().compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0) return false;
        if (bd.abs().compareTo(BigDecimal.valueOf(Double.MIN_NORMAL)) < 0 && bd.signum() != 0) return false;

        int digits = bd.precision();
        return digits <= 16;
    }

    public static NElementType commonElementNumberType(Class<? extends Number> aa, Class<? extends Number> bb) {
        if (aa == null && bb == null) {
            return null;
        }
        NElementType at = elementNumberType(aa, null);
        NElementType bt = elementNumberType(bb, null);
        if (aa == null) {
            return bt;
        }
        if (bb == null) {
            return at;
        }
        if (at == null && bt == null) {
            return null;
        }
        if (at == null) {
            return bt;
        }
        if (bt == null) {
            return at;
        }
        switch (at) {
            case BIG_COMPLEX: {
                switch (at) {
                    case BIG_COMPLEX:
                    case DOUBLE_COMPLEX:
                    case FLOAT_COMPLEX:
                    case BIG_DECIMAL:
                    case BIG_INT:
                    case DOUBLE:
                    case FLOAT:
                    case LONG:
                    case INT:
                    case SHORT:
                    case BYTE:
                    default:
                        return NElementType.BIG_COMPLEX;
                }
            }
            case DOUBLE_COMPLEX: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.DOUBLE_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.DOUBLE_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_COMPLEX;
                    case BIG_INT:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE:
                        return NElementType.DOUBLE_COMPLEX;
                    case FLOAT:
                        return NElementType.DOUBLE_COMPLEX;
                    case LONG:
                        return NElementType.DOUBLE_COMPLEX;
                    case INT:
                        return NElementType.DOUBLE_COMPLEX;
                    case SHORT:
                        return NElementType.DOUBLE_COMPLEX;
                    case BYTE:
                        return NElementType.DOUBLE_COMPLEX;
                    default:
                        return NElementType.DOUBLE_COMPLEX;
                }
            }
            case FLOAT_COMPLEX: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.DOUBLE_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.FLOAT_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_COMPLEX;
                    case BIG_INT:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE:
                        return NElementType.DOUBLE_COMPLEX;
                    case FLOAT:
                        return NElementType.FLOAT_COMPLEX;
                    case LONG:
                        return NElementType.FLOAT_COMPLEX;
                    case INT:
                        return NElementType.FLOAT_COMPLEX;
                    case SHORT:
                        return NElementType.FLOAT_COMPLEX;
                    case BYTE:
                        return NElementType.FLOAT_COMPLEX;
                    default:
                        return NElementType.FLOAT_COMPLEX;
                }
            }
            case BIG_DECIMAL: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_DECIMAL;
                    case BIG_INT:
                        return NElementType.BIG_DECIMAL;
                    case DOUBLE:
                        return NElementType.BIG_DECIMAL;
                    case FLOAT:
                        return NElementType.BIG_DECIMAL;
                    case LONG:
                        return NElementType.BIG_DECIMAL;
                    case INT:
                        return NElementType.BIG_DECIMAL;
                    case SHORT:
                        return NElementType.BIG_DECIMAL;
                    case BYTE:
                        return NElementType.BIG_DECIMAL;
                    default:
                        return NElementType.BIG_DECIMAL;
                }
            }
            case BIG_INT: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_DECIMAL;
                    case BIG_INT:
                        return NElementType.BIG_INT;
                    case DOUBLE:
                        return NElementType.BIG_INT;
                    case FLOAT:
                        return NElementType.BIG_INT;
                    case LONG:
                        return NElementType.BIG_INT;
                    case INT:
                        return NElementType.BIG_INT;
                    case SHORT:
                        return NElementType.BIG_INT;
                    case BYTE:
                        return NElementType.BIG_INT;
                    default:
                        return NElementType.BIG_INT;
                }
            }
            case DOUBLE: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_DECIMAL;
                    case BIG_INT:
                        return NElementType.BIG_DECIMAL;
                    case DOUBLE:
                        return NElementType.DOUBLE;
                    case FLOAT:
                        return NElementType.DOUBLE;
                    case LONG:
                        return NElementType.DOUBLE;
                    case INT:
                        return NElementType.DOUBLE;
                    case SHORT:
                        return NElementType.DOUBLE;
                    case BYTE:
                        return NElementType.DOUBLE;
                    default:
                        return NElementType.DOUBLE;
                }
            }
            case FLOAT: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_DECIMAL;
                    case BIG_INT:
                        return NElementType.BIG_DECIMAL;
                    case DOUBLE:
                        return NElementType.DOUBLE;
                    case FLOAT:
                        return NElementType.FLOAT;
                    case LONG:
                        return NElementType.DOUBLE;
                    case INT:
                        return NElementType.FLOAT;
                    case SHORT:
                        return NElementType.FLOAT;
                    case BYTE:
                        return NElementType.FLOAT;
                    default:
                        return NElementType.FLOAT;
                }
            }
            case LONG: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_DECIMAL;
                    case BIG_INT:
                        return NElementType.BIG_DECIMAL;
                    case DOUBLE:
                        return NElementType.DOUBLE;
                    case FLOAT:
                        return NElementType.DOUBLE;
                    case LONG:
                        return NElementType.LONG;
                    case INT:
                        return NElementType.LONG;
                    case SHORT:
                        return NElementType.LONG;
                    case BYTE:
                        return NElementType.LONG;
                    default:
                        return NElementType.LONG;
                }
            }
            case INT: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_DECIMAL;
                    case BIG_INT:
                        return NElementType.BIG_DECIMAL;
                    case DOUBLE:
                        return NElementType.DOUBLE;
                    case FLOAT:
                        return NElementType.DOUBLE;
                    case LONG:
                        return NElementType.LONG;
                    case INT:
                        return NElementType.INT;
                    case SHORT:
                        return NElementType.INT;
                    case BYTE:
                        return NElementType.INT;
                    default:
                        return NElementType.INT;
                }
            }
            case SHORT: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_DECIMAL;
                    case BIG_INT:
                        return NElementType.BIG_DECIMAL;
                    case DOUBLE:
                        return NElementType.DOUBLE;
                    case FLOAT:
                        return NElementType.DOUBLE;
                    case LONG:
                        return NElementType.LONG;
                    case INT:
                        return NElementType.INT;
                    case SHORT:
                        return NElementType.SHORT;
                    case BYTE:
                        return NElementType.SHORT;
                    default:
                        return NElementType.SHORT;
                }
            }
            case BYTE: {
                switch (at) {
                    case BIG_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case DOUBLE_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case FLOAT_COMPLEX:
                        return NElementType.BIG_COMPLEX;
                    case BIG_DECIMAL:
                        return NElementType.BIG_DECIMAL;
                    case BIG_INT:
                        return NElementType.BIG_DECIMAL;
                    case DOUBLE:
                        return NElementType.DOUBLE;
                    case FLOAT:
                        return NElementType.DOUBLE;
                    case LONG:
                        return NElementType.LONG;
                    case INT:
                        return NElementType.INT;
                    case SHORT:
                        return NElementType.SHORT;
                    case BYTE:
                        return NElementType.BYTE;
                    default:
                        return NElementType.BYTE;
                }
            }
            default:
                return at;
        }
    }

    public static Class<? extends Number> commonNumberType(Class<? extends Number> aa, Class<? extends Number> bb) {
        if (aa == null && bb == null) {
            return Number.class;
        }
        if (aa == null) {
            return bb;
        }
        if (bb == null) {
            return aa;
        }
        if (NBigComplex.class.equals(aa) || NBigComplex.class.equals(bb)) {
            return NBigComplex.class;
        }
        if (NDoubleComplex.class.equals(aa) || NDoubleComplex.class.equals(bb)) {
            if (BigInteger.class.equals(aa) || BigInteger.class.equals(bb)) {
                return NBigComplex.class;
            }
            return NDoubleComplex.class;
        }
        if (NFloatComplex.class.equals(aa) || NFloatComplex.class.equals(bb)) {
            if (BigInteger.class.equals(aa) || BigInteger.class.equals(bb)) {
                return NBigComplex.class;
            }
            if (Long.class.equals(aa) || Long.class.equals(bb)) {
                return NDoubleComplex.class;
            }
            return NFloatComplex.class;
        }
        if (BigDecimal.class.equals(aa) || BigDecimal.class.equals(bb)) {
            return BigDecimal.class;
        }
        if (BigInteger.class.equals(aa) || BigInteger.class.equals(bb)) {
            if (Double.class.equals(aa) || Double.class.equals(bb) || Float.class.equals(aa) || Float.class.equals(bb)) {
                return BigDecimal.class;
            }
            return BigInteger.class;
        }
        if (Double.class.equals(aa) || Double.class.equals(bb)) {
            return Double.class;
        }
        if (Float.class.equals(aa) || Float.class.equals(bb)) {
            if (Long.class.equals(aa) || Long.class.equals(bb)) {
                return Double.class;
            }
            return Float.class;
        }
        if (Long.class.equals(aa) || Long.class.equals(bb)) {
            return Long.class;
        }
        if (Integer.class.equals(aa) || Integer.class.equals(bb)) {
            return Integer.class;
        }
        if (Short.class.equals(aa) || Short.class.equals(bb)) {
            return Short.class;
        }
        if (Byte.class.equals(aa) || Byte.class.equals(bb)) {
            return Byte.class;
        }
        return Number.class;
    }

//    public static int compareNumbers(Number a, Number b) {
//        if (a == null && b == null) {
//            return 0;
//        }
//        if (a == null) {
//            return -1;
//        }
//        if (b == null) {
//            return 1;
//        }
//        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
//        switch (ct.getName()) {
//            case "java.lang.Byte":
//            case "java.lang.Short":
//            case "java.lang.Integer":
//            case "java.lang.Long": {
//                return Long.compare(a.longValue(), b.longValue());
//            }
//            case "java.lang.Float":
//            case "java.lang.Double": {
//                return Double.compare(a.doubleValue(), b.doubleValue());
//            }
//            case "java.math.BigInteger": {
//                return NLiteral.of(a).asBigInt().get().compareTo(NLiteral.of(b).asBigInt().get());
//            }
//            case "java.math.BigDecimal": {
//                return NLiteral.of(a).asBigDecimal().get().compareTo(NLiteral.of(b).asBigDecimal().get());
//            }
//            case "net.thevpc.nuts.math.NFloatComplex": {
//                return NLiteral.of(a).asFloatComplex().get().compareTo(NLiteral.of(b).asFloatComplex().get());
//            }
//            case "net.thevpc.nuts.math.NDoubleComplex": {
//                return NLiteral.of(a).asDoubleComplex().get().compareTo(NLiteral.of(b).asDoubleComplex().get());
//            }
//            case "net.thevpc.nuts.math.NBigComplex": {
//                return NLiteral.of(a).asBigComplex().get().compareTo(NLiteral.of(b).asBigComplex().get());
//            }
//        }
//        return String.valueOf(a).compareTo(String.valueOf(b));
//    }

    public static Number addNumbers(Number a, Number b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
                return (byte) (NLiteral.of(a).asByte().get() + NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
                return (short) (NLiteral.of(a).asShort().get() + NLiteral.of(b).asShort().get());
            }
            case "java.lang.Integer": {
                return NLiteral.of(a).asInt().get() + NLiteral.of(b).asInt().get();
            }
            case "java.lang.Long": {
                return NLiteral.of(a).asLong().get() + NLiteral.of(b).asLong().get();
            }
            case "java.lang.Float": {
                return NLiteral.of(a).asFloat().get() + NLiteral.of(b).asFloat().get();
            }
            case "java.lang.Double": {
                return NLiteral.of(a).asDouble().get() + NLiteral.of(b).asDouble().get();
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().add(NLiteral.of(b).asBigInt().get());
            }
            case "java.math.BigDecimal": {
                return NLiteral.of(a).asBigDecimal().get().add(NLiteral.of(b).asBigDecimal().get());
            }
            case "net.thevpc.nuts.math.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().add(NLiteral.of(b).asFloatComplex().get());
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().add(NLiteral.of(b).asDoubleComplex().get());
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().add(NLiteral.of(b).asBigComplex().get());
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    public static Number substructNumbers(Number a, Number b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return negateNumber(b);
        }
        if (b == null) {
            return a;
        }
        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
                return (byte) (NLiteral.of(a).asByte().get() - NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
                return (short) (NLiteral.of(a).asShort().get() - NLiteral.of(b).asShort().get());
            }
            case "java.lang.Integer": {
                return NLiteral.of(a).asInt().get() - NLiteral.of(b).asInt().get();
            }
            case "java.lang.Long": {
                return NLiteral.of(a).asLong().get() - NLiteral.of(b).asLong().get();
            }
            case "java.lang.Float": {
                return NLiteral.of(a).asFloat().get() - NLiteral.of(b).asFloat().get();
            }
            case "java.lang.Double": {
                return NLiteral.of(a).asDouble().get() - NLiteral.of(b).asDouble().get();
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().subtract(NLiteral.of(b).asBigInt().get());
            }
            case "java.math.BigDecimal": {
                return NLiteral.of(a).asBigDecimal().get().subtract(NLiteral.of(b).asBigDecimal().get());
            }
            case "net.thevpc.nuts.math.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().subtract(NLiteral.of(b).asFloatComplex().get());
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().subtract(NLiteral.of(b).asDoubleComplex().get());
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().subtract(NLiteral.of(b).asBigComplex().get());
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    public static Number multiplyNumbers(Number a, Number b, MathContext mc) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
                return (byte) (NLiteral.of(a).asByte().get() * NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
                return (short) (NLiteral.of(a).asShort().get() * NLiteral.of(b).asShort().get());
            }
            case "java.lang.Integer": {
                return NLiteral.of(a).asInt().get() * NLiteral.of(b).asInt().get();
            }
            case "java.lang.Long": {
                return NLiteral.of(a).asLong().get() * NLiteral.of(b).asLong().get();
            }
            case "java.lang.Float": {
                return NLiteral.of(a).asFloat().get() * NLiteral.of(b).asFloat().get();
            }
            case "java.lang.Double": {
                return NLiteral.of(a).asDouble().get() * NLiteral.of(b).asDouble().get();
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().multiply(NLiteral.of(b).asBigInt().get());
            }
            case "java.math.BigDecimal": {
                return NLiteral.of(a).asBigDecimal().get().multiply(NLiteral.of(b).asBigDecimal().get());
            }
            case "net.thevpc.nuts.math.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().multiply(NLiteral.of(b).asFloatComplex().get());
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().multiply(NLiteral.of(b).asDoubleComplex().get());
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().multiply(NLiteral.of(b).asBigComplex().get(), mc);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    public static Number divideNumbers(Number a, Number b, MathContext mc) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
                return (byte) (NLiteral.of(a).asByte().get() / NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
                return (short) (NLiteral.of(a).asShort().get() / NLiteral.of(b).asShort().get());
            }
            case "java.lang.Integer": {
                return NLiteral.of(a).asInt().get() / NLiteral.of(b).asInt().get();
            }
            case "java.lang.Long": {
                return NLiteral.of(a).asLong().get() / NLiteral.of(b).asLong().get();
            }
            case "java.lang.Float": {
                return NLiteral.of(a).asFloat().get() / NLiteral.of(b).asFloat().get();
            }
            case "java.lang.Double": {
                return NLiteral.of(a).asDouble().get() / NLiteral.of(b).asDouble().get();
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().divide(NLiteral.of(b).asBigInt().get());
            }
            case "java.math.BigDecimal": {
                return NLiteral.of(a).asBigDecimal().get().divide(NLiteral.of(b).asBigDecimal().get(), RoundingMode.HALF_EVEN);
            }
            case "net.thevpc.nuts.math.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().divide(NLiteral.of(b).asFloatComplex().get());
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().divide(NLiteral.of(b).asDoubleComplex().get());
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().divide(NLiteral.of(b).asBigComplex().get(), mc);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    public static Number reminderNumbers(Number a, Number b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
                return (byte) (NLiteral.of(a).asByte().get() % NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
                return (short) (NLiteral.of(a).asShort().get() % NLiteral.of(b).asShort().get());
            }
            case "java.lang.Integer": {
                return NLiteral.of(a).asInt().get() % NLiteral.of(b).asInt().get();
            }
            case "java.lang.Long": {
                return NLiteral.of(a).asLong().get() % NLiteral.of(b).asLong().get();
            }
            case "java.lang.Float": {
                return NLiteral.of(a).asFloat().get() % NLiteral.of(b).asFloat().get();
            }
            case "java.lang.Double": {
                return NLiteral.of(a).asDouble().get() % NLiteral.of(b).asDouble().get();
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().remainder(NLiteral.of(b).asBigInt().get());
            }
            case "java.math.BigDecimal": {
                return NLiteral.of(a).asBigDecimal().get().remainder(NLiteral.of(b).asBigDecimal().get());
            }
//            case "net.thevpc.nuts.math.NFloatComplex": {
//                return NLiteral.of(a).asFloatComplex().get().remainder(NLiteral.of(b).asFloatComplex().get());
//            }
//            case "net.thevpc.nuts.math.NDoubleComplex": {
//                return NLiteral.of(a).asDoubleComplex().get().remainder(NLiteral.of(b).asDoubleComplex().get());
//            }
//            case "net.thevpc.nuts.math.NBigComplex": {
//                return NLiteral.of(a).asBigComplex().get().remainder(NLiteral.of(b).asBigComplex().get());
//            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    public static Number powerNumbers(Number a, Number b, MathContext mc) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            a = (byte) 0;
        }
        if (b == null) {
            return a;
        }
        Class<? extends Number> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
                return (byte) Math.pow(NLiteral.of(a).asByte().get(), NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
                return (short) Math.pow(NLiteral.of(a).asShort().get(), NLiteral.of(b).asShort().get());
            }
            case "java.lang.Integer": {
                return (int) Math.pow(NLiteral.of(a).asInt().get(), NLiteral.of(b).asInt().get());
            }
            case "java.lang.Long": {
                return (long) Math.pow(NLiteral.of(a).asLong().get(), NLiteral.of(b).asLong().get());
            }
            case "java.lang.Float": {
                return (float) Math.pow(NLiteral.of(a).asFloat().get(), NLiteral.of(b).asFloat().get());
            }
            case "java.lang.Double": {
                return Math.pow(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().pow(NLiteral.of(b).asInt().get());
            }
            case "java.math.BigDecimal": {
                return BigDecimal.valueOf(Math.pow(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get()));
            }
//            case "net.thevpc.nuts.math.NFloatComplex": {
//                return NLiteral.of(a).asFloatComplex().get().pow(NLiteral.of(b).asFloatComplex().get());
//            }
//            case "net.thevpc.nuts.math.NDoubleComplex": {
//                return NLiteral.of(a).asDoubleComplex().get().pow(NLiteral.of(b).asDoubleComplex().get());
//            }
//            case "net.thevpc.nuts.math.NBigComplex": {
//                return NLiteral.of(a).asBigComplex().get().pow(NLiteral.of(b).asBigComplex().get());
//            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    public static Number negateNumber(Number a) {
        if (a == null) {
            return null;
        }
        switch (a.getClass().getName()) {
            case "java.lang.Byte": {
                return (byte) (-NLiteral.of(a).asByte().get());
            }
            case "java.lang.Short": {
                return (short) (-NLiteral.of(a).asShort().get());
            }
            case "java.lang.Integer": {
                return -NLiteral.of(a).asInt().get();
            }
            case "java.lang.Long": {
                return -NLiteral.of(a).asLong().get();
            }
            case "java.lang.Float": {
                return -NLiteral.of(a).asFloat().get();
            }
            case "java.lang.Double": {
                return -NLiteral.of(a).asDouble().get();
            }
            case "java.math.BigInteger": {
                return NLiteral.of(a).asBigInt().get().negate();
            }
            case "java.math.BigDecimal": {
                return NLiteral.of(a).asBigDecimal().get().negate();
            }
            case "net.thevpc.nuts.math.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().negate();
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().negate();
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().negate();
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", a.getClass()));
    }

    public static Number invNumber(Number a, MathContext mc) {
        if (a == null) {
            return null;
        }
        switch (a.getClass().getName()) {
            case "java.lang.Byte": {
                return 1.0 / (NLiteral.of(a).asDouble().get());
            }
            case "java.lang.Short": {
                return 1.0 / (NLiteral.of(a).asDouble().get());
            }
            case "java.lang.Integer": {
                return 1.0 / (NLiteral.of(a).asDouble().get());
            }
            case "java.lang.Long": {
                return 1.0 / (NLiteral.of(a).asDouble().get());
            }
            case "java.lang.Float": {
                return 1.0f / NLiteral.of(a).asFloat().get();
            }
            case "java.lang.Double": {
                return 1.0 / NLiteral.of(a).asDouble().get();
            }
            case "java.math.BigInteger": {
                mc= getContextMathContext(mc);
                return BigDecimal.ONE.divide(NLiteral.of(a).asBigDecimal().get(), mc);
            }
            case "java.math.BigDecimal": {
                mc= getContextMathContext(mc);
                return BigDecimal.ONE.divide(NLiteral.of(a).asBigDecimal().get(), mc);
            }
            case "net.thevpc.nuts.math.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().inv();
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().inv();
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().inv(mc);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", a.getClass()));
    }
}
