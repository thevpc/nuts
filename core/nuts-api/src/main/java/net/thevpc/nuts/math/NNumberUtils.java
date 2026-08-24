package net.thevpc.nuts.math;

import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * NNumberUtils class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NNumberUtils {
    public static ThreadLocal<MathContext> MATH_CONTEXT = new ThreadLocal<>();

    /**
     * Call with math context.
     *
     * @param mc mc
     * @param c c
     * @return call with math context result
     */
    public static <T> T callWithMathContext(MathContext mc, NCallable<T> c) {
        MathContext o = MATH_CONTEXT.get();
        MATH_CONTEXT.set(mc);
        try {
            return c.call();
        } finally {
            MATH_CONTEXT.set(o);
        }
    }

    /**
     * Context math context.
     *
     * @param user user
     * @return context math context result
     */
    public static MathContext contextMathContext(MathContext user) {
        if (user != null) {
            return user;
        }
        /**
         * Context math context.
         *
         * @return context math context result
         */
        return contextMathContext();
    }

    /**
     * Context math context.
     *
     * @return context math context result
     */
    public static MathContext contextMathContext() {
        MathContext a = MATH_CONTEXT.get();
        return a == null ? MathContext.DECIMAL128 : a;
    }

    /**
     * Run with math context.
     *
     * @param mc mc
     * @param c c
     */
    public static void runWithMathContext(MathContext mc, Runnable c) {
        MathContext o = MATH_CONTEXT.get();
        MATH_CONTEXT.set(mc);
        try {
            c.run();
        } finally {
            MATH_CONTEXT.set(o);
        }
    }

    /**
     * Element number type.
     *
     * @param aa aa
     * @param def def
     * @return element number type result
     */
    public static NElementType elementNumberType(Class<? extends Number> aa, NElementType def) {
        if (aa == null) {
            return null;
        }
        switch (aa.getName()) {
            case "java.math.BigDecimal":
                return NElementType.BIG_DECIMAL;
            case "java.math.BigInteger":
                return NElementType.BIG_INT;
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

    /**
     * Xor doubles.
     *
     * @param a a
     * @param b b
     * @return xor doubles result
     */
    public static double xorDoubles(double a, double b) {
        long aBits = Double.doubleToRawLongBits(a);
        long bBits = Double.doubleToRawLongBits(b);
        long xorBits = aBits ^ bBits;
        return Double.longBitsToDouble(xorBits);
    }

    /**
     * Xor floats.
     *
     * @param a a
     * @param b b
     * @return xor floats result
     */
    public static float xorFloats(float a, float b) {
        int aBits = Float.floatToRawIntBits(a);
        int bBits = Float.floatToRawIntBits(b);
        int xorBits = aBits ^ bBits;
        return Float.intBitsToFloat(xorBits);
    }

    /**
     * Xor numbers.
     *
     * @param a a
     * @param b b
     * @return xor numbers result
     */
    public static Number xorNumbers(Number a, Number b) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                /**
                 * Xor big decimals.
                 *
                 * @param NLiteral.of(a).asBigDecimal().get() n literal.of(a).as big decimal().get()
                 * @param NLiteral.of(b).asBigDecimal().get() n literal.of(b).as big decimal().get()
                 * @return xor big decimals result
                 */
                return xorBigDecimals(NLiteral.of(a).asBigDecimal().get(), NLiteral.of(b).asBigDecimal().get());
            case BIG_INT:
                return NLiteral.of(a).asBigInt().get().xor(NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                /**
                 * Xor doubles.
                 *
                 * @param NLiteral.of(a).asDouble().get() n literal.of(a).as double().get()
                 * @param NLiteral.of(b).asDouble().get() n literal.of(b).as double().get()
                 * @return xor doubles result
                 */
                return xorDoubles(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case FLOAT:
                /**
                 * Xor floats.
                 *
                 * @param NLiteral.of(a).asFloat().get() n literal.of(a).as float().get()
                 * @param NLiteral.of(b).asFloat().get() n literal.of(b).as float().get()
                 * @return xor floats result
                 */
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
        /**
         * N illegal argument exception.
         *
         * @param b) b)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unable to xor numbers %s and %s", a, b));
    }

    /**
     * Or numbers.
     *
     * @param a a
     * @param b b
     * @return or numbers result
     */
    public static Number orNumbers(Number a, Number b) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                /**
                 * Or big decimals.
                 *
                 * @param NLiteral.of(a).asBigDecimal().get() n literal.of(a).as big decimal().get()
                 * @param NLiteral.of(b).asBigDecimal().get() n literal.of(b).as big decimal().get()
                 * @return or big decimals result
                 */
                return orBigDecimals(NLiteral.of(a).asBigDecimal().get(), NLiteral.of(b).asBigDecimal().get());
            case BIG_INT:
                return NLiteral.of(a).asBigInt().get().or(NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                /**
                 * Or doubles.
                 *
                 * @param NLiteral.of(a).asDouble().get() n literal.of(a).as double().get()
                 * @param NLiteral.of(b).asDouble().get() n literal.of(b).as double().get()
                 * @return or doubles result
                 */
                return orDoubles(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case FLOAT:
                /**
                 * Or floats.
                 *
                 * @param NLiteral.of(a).asFloat().get() n literal.of(a).as float().get()
                 * @param NLiteral.of(b).asFloat().get() n literal.of(b).as float().get()
                 * @return or floats result
                 */
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
        /**
         * N illegal argument exception.
         *
         * @param b) b)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unable to or numbers %s and %s", a, b));
    }

    /**
     * And numbers.
     *
     * @param a a
     * @param b b
     * @return and numbers result
     */
    public static Number andNumbers(Number a, Number b) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                /**
                 * And big decimals.
                 *
                 * @param NLiteral.of(a).asBigDecimal().get() n literal.of(a).as big decimal().get()
                 * @param NLiteral.of(b).asBigDecimal().get() n literal.of(b).as big decimal().get()
                 * @return and big decimals result
                 */
                return andBigDecimals(NLiteral.of(a).asBigDecimal().get(), NLiteral.of(b).asBigDecimal().get());
            case BIG_INT:
                return NLiteral.of(a).asBigInt().get().and(NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                /**
                 * And doubles.
                 *
                 * @param NLiteral.of(a).asDouble().get() n literal.of(a).as double().get()
                 * @param NLiteral.of(b).asDouble().get() n literal.of(b).as double().get()
                 * @return and doubles result
                 */
                return andDoubles(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case FLOAT:
                /**
                 * And floats.
                 *
                 * @param NLiteral.of(a).asFloat().get() n literal.of(a).as float().get()
                 * @param NLiteral.of(b).asFloat().get() n literal.of(b).as float().get()
                 * @return and floats result
                 */
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
        /**
         * N illegal argument exception.
         *
         * @param b) b)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unable to 'and' numbers %s and %s", a, b));
    }

    public static boolean equals(Number a, Number b) {
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
        /**
         * N illegal argument exception.
         *
         * @param b) b)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unable to 'eq'' numbers %s and %s", a, b));
    }

    public static boolean equals(Number a, Number b, double epsilon) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            {
                NBigComplex aa = (NBigComplex) a;
                NBigComplex bb = (NBigComplex) b;
                BigDecimal ar = aa.realValue(), ai = aa.imagValue();
                BigDecimal br = bb.realValue(), bi = bb.imagValue();
                BigDecimal diffR = ar.subtract(br).abs();
                BigDecimal diffI = ai.subtract(bi).abs();
                if (diffR.signum() == 0 && diffI.signum() == 0) return true;
                BigDecimal mag = ar.abs().max(ai.abs()).max(br.abs()).max(bi.abs());
                if (mag.signum() == 0) return true;
                double rR = Math.abs(diffR.divide(mag, MathContext.DECIMAL64).doubleValue());
                double rI = Math.abs(diffI.divide(mag, MathContext.DECIMAL64).doubleValue());
                return rR <= Math.abs(epsilon) && rI <= Math.abs(epsilon);
            }
            case DOUBLE_COMPLEX:{
                NDoubleComplex aa = (NDoubleComplex) a;
                NDoubleComplex bb = (NDoubleComplex) b;
                double ar = aa.realValue(), ai = aa.imagValue();
                double br = bb.realValue(), bi = bb.imagValue();
                double mag = Math.max(
                        Math.max(Math.abs(ar), Math.abs(ai)),
                        Math.max(Math.abs(br), Math.abs(bi))
                );
                double diffR = Math.abs(ar - br);
                double diffI = Math.abs(ai - bi);
                if (mag == 0) return diffR == 0 && diffI == 0;
                return diffR / mag < epsilon && diffI / mag < epsilon;
            }
            case FLOAT_COMPLEX:{
                NFloatComplex aa = (NFloatComplex) a;
                NFloatComplex bb = (NFloatComplex) b;
                double ar = aa.realValue(), ai = aa.imagValue();
                double br = bb.realValue(), bi = bb.imagValue();
                double mag = Math.max(
                        Math.max(Math.abs(ar), Math.abs(ai)),
                        Math.max(Math.abs(br), Math.abs(bi))
                );
                double diffR = Math.abs(ar - br);
                double diffI = Math.abs(ai - bi);
                if (mag == 0) return diffR == 0 && diffI == 0;
                return diffR / mag < epsilon && diffI / mag < epsilon;
            }
            case BIG_DECIMAL:
            {
                BigDecimal aa = NLiteral.of(a).asBigDecimal().get();
                BigDecimal bb = NLiteral.of(b).asBigDecimal().get();
                BigDecimal diff = aa.subtract(bb).abs();
                if (diff.signum() == 0) return true;
                BigDecimal mag = aa.abs().max(bb.abs());
                if (mag.signum() == 0) return true;
                return Math.abs(diff.divide(mag, MathContext.DECIMAL64).doubleValue()) <= Math.abs(epsilon);
            }
            case BIG_INT:
            case ULONG:
            {
                BigInteger aa = NLiteral.of(a).asBigInt().get();
                BigInteger bb = NLiteral.of(b).asBigInt().get();
                return aa.equals(bb);
            }
            case DOUBLE:
            case FLOAT:
            {
                double aa = NLiteral.of(a).asDouble().get();
                double bb = NLiteral.of(b).asDouble().get();
                double mag  = Math.max(Math.abs(aa), Math.abs(bb));
                double diff = Math.abs(aa-bb);
                return mag == 0 ? diff == 0 : diff / mag < epsilon;            }
            case LONG:
            case INT:
            case UINT:
            case SHORT:
            case USHORT:
            case BYTE:
            case UBYTE:
                return NLiteral.of(a).asLong().get().equals(NLiteral.of(b).asLong().get());
        }
        /**
         * N illegal argument exception.
         *
         * @param b) b)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unable to 'like' numbers %s and %s", a, b));
    }

    /**
     * Compare numbers.
     *
     * @param a a
     * @param b b
     * @return compare numbers result
     */
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
        /**
         * N illegal argument exception.
         *
         * @param b) b)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unable to 'compare'' numbers %s and %s", a, b));
    }

    /**
     * Or big decimals.
     *
     * @param a a
     * @param b b
     * @return or big decimals result
     */
    public static BigDecimal orBigDecimals(BigDecimal a, BigDecimal b) {
        return new BigDecimal(a.toBigInteger().or(b.toBigInteger()));
    }

    /**
     * And big decimals.
     *
     * @param a a
     * @param b b
     * @return and big decimals result
     */
    public static BigDecimal andBigDecimals(BigDecimal a, BigDecimal b) {
        return new BigDecimal(a.toBigInteger().and(b.toBigInteger()));
    }

    /**
     * Or doubles.
     *
     * @param a a
     * @param b b
     * @return or doubles result
     */
    public static double orDoubles(double a, double b) {
        return Double.longBitsToDouble(
                Double.doubleToLongBits(a) | Double.doubleToLongBits(b)
        );
    }

    /**
     * And doubles.
     *
     * @param a a
     * @param b b
     * @return and doubles result
     */
    public static double andDoubles(double a, double b) {
        return Double.longBitsToDouble(
                Double.doubleToLongBits(a) & Double.doubleToLongBits(b)
        );
    }

    /**
     * Or floats.
     *
     * @param a a
     * @param b b
     * @return or floats result
     */
    public static float orFloats(float a, float b) {
        return Float.intBitsToFloat(Float.floatToIntBits(a) | Float.floatToIntBits(b));
    }

    /**
     * And floats.
     *
     * @param a a
     * @param b b
     * @return and floats result
     */
    public static float andFloats(float a, float b) {
        return Float.intBitsToFloat(Float.floatToIntBits(a) & Float.floatToIntBits(b));
    }

    /**
     * Pow longs.
     *
     * @param base base
     * @param exponent exponent
     * @return pow longs result
     */
    public static long powLongs(long base, long exponent) {
        if (exponent < 0) {
            /**
             * Arithmetic exception.
             *
             * @param long" long"
             * @return arithmetic exception result
             */
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

    /**
     * Pow big ints.
     *
     * @param base base
     * @param exponent exponent
     * @return pow big ints result
     */
    public static BigInteger powBigInts(BigInteger base, BigInteger exponent) {
        if (exponent.signum() < 0) {
            /**
             * Arithmetic exception.
             *
             * @param BigInteger" big integer"
             * @return arithmetic exception result
             */
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

    /**
     * Pow ints.
     *
     * @param base base
     * @param exponent exponent
     * @return pow ints result
     */
    public static int powInts(int base, int exponent) {
        if (exponent < 0) {
            /**
             * Arithmetic exception.
             *
             * @param int" int"
             * @return arithmetic exception result
             */
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

    /**
     * Pow numbers.
     *
     * @param a a
     * @param b b
     * @param mc mc
     * @return pow numbers result
     */
    public static Number powNumbers(Number a, Number b, MathContext mc) {
        Class<? extends Number> at = a.getClass();
        Class<? extends Number> bt = b.getClass();
        NElementType e = commonElementNumberType(at, bt);
        switch (e) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_DECIMAL:
                /**
                 * Pow big decimals.
                 *
                 * @param NLiteral.of(a).asBigDecimal().get() n literal.of(a).as big decimal().get()
                 * @param NLiteral.of(b).asBigDecimal().get() n literal.of(b).as big decimal().get()
                 * @param mc mc
                 * @return pow big decimals result
                 */
                return powBigDecimals(NLiteral.of(a).asBigDecimal().get(), NLiteral.of(b).asBigDecimal().get(), mc);
            case BIG_INT:
                /**
                 * Pow big ints.
                 *
                 * @param NLiteral.of(a).asBigInt().get() n literal.of(a).as big int().get()
                 * @param NLiteral.of(b).asBigInt().get() n literal.of(b).as big int().get()
                 * @return pow big ints result
                 */
                return powBigInts(NLiteral.of(a).asBigInt().get(), NLiteral.of(b).asBigInt().get());
            case DOUBLE:
                return Math.pow(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case FLOAT:
              /**
               * Return.
               *
               * @param Math.pow(NLiteral.of(a).asDouble().get() math.pow(n literal.of(a).as double().get()
               * @param NLiteral.of(b).asDouble().get() n literal.of(b).as double().get()
               */
                return (float) Math.pow(NLiteral.of(a).asDouble().get(), NLiteral.of(b).asDouble().get());
            case LONG:
                /**
                 * Pow longs.
                 *
                 * @param NLiteral.of(a).asLong().get() n literal.of(a).as long().get()
                 * @param NLiteral.of(b).asLong().get() n literal.of(b).as long().get()
                 * @return pow longs result
                 */
                return powLongs(NLiteral.of(a).asLong().get(), NLiteral.of(b).asLong().get());
            case INT:
                /**
                 * Pow ints.
                 *
                 * @param NLiteral.of(a).asInt().get() n literal.of(a).as int().get()
                 * @param NLiteral.of(b).asInt().get() n literal.of(b).as int().get()
                 * @return pow ints result
                 */
                return powInts(NLiteral.of(a).asInt().get(), NLiteral.of(b).asInt().get());
            case SHORT:
              /**
               * Return.
               *
               * @param powInts(NLiteral.of(a).asInt().get() pow ints(n literal.of(a).as int().get()
               * @param NLiteral.of(b).asInt().get() n literal.of(b).as int().get()
               */
                return (short) powInts(NLiteral.of(a).asInt().get(), NLiteral.of(b).asInt().get());
            case BYTE:
              /**
               * Return.
               *
               * @param powInts(NLiteral.of(a).asInt().get() pow ints(n literal.of(a).as int().get()
               * @param NLiteral.of(b).asInt().get() n literal.of(b).as int().get()
               */
                return (byte) powInts(NLiteral.of(a).asInt().get(), NLiteral.of(b).asInt().get());
        }
        /**
         * N illegal argument exception.
         *
         * @param b) b)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unable to xor numbers %s and %s", a, b));
    }

    /**
     * Xor big decimals.
     *
     * @param aa aa
     * @param bb bb
     * @return xor big decimals result
     */
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

    /**
     * Pow big decimals.
     *
     * @param x x
     * @param y y
     * @param mc mc
     * @return pow big decimals result
     */
    public static BigDecimal powBigDecimals(BigDecimal x, BigDecimal y, MathContext mc) {
        mc = contextMathContext(mc);

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
        /**
         * Exp.
         *
         * @param yLnX y ln x
         * @param mc mc
         * @return exp result
         */
        return exp(yLnX, mc);      // requires BigDecimalMath library
    }

    /**
     * Exp.
     *
     * @param x x
     * @param mc mc
     * @return exp result
     */
    public static BigDecimal exp(BigDecimal x, MathContext mc) {
        BigDecimal sum = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        mc = contextMathContext(mc);
        int n = 1;
        while (term.compareTo(BigDecimal.ZERO) != 0) {
            term = term.multiply(x, mc).divide(BigDecimal.valueOf(n), mc);
            sum = sum.add(term, mc);
            n++;
            if (term.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision())) < 0) break;
        }
        return sum;
    }

    /**
     * Ln big decimals.
     *
     * @param x x
     * @param mc mc
     * @return ln big decimals result
     */
    public static BigDecimal lnBigDecimals(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) <= 0) throw new ArithmeticException("ln(x) undefined for x <= 0");
        mc = contextMathContext(mc);

        BigDecimal two = BigDecimal.valueOf(2);
        BigDecimal y = x.subtract(BigDecimal.ONE).divide(x.add(BigDecimal.ONE), mc);
        BigDecimal y2 = y.multiply(y, mc);

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal term = y;
        int n = 0;
        while (term.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision())) > 0) {
            BigDecimal denom = BigDecimal.valueOf(2L * n + 1);
            sum = sum.add(term.divide(denom, mc), mc);
            term = term.multiply(y2, mc);
            n++;
        }
        return sum.multiply(two, mc);
    }

    /**
     * Checks if can use double.
     *
     * @param bd bd
     * @return can use double result
     */
    public static boolean canUseDouble(BigDecimal bd) {
        if (bd.abs().compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0) return false;
        if (bd.abs().compareTo(BigDecimal.valueOf(Double.MIN_NORMAL)) < 0 && bd.signum() != 0) return false;

        int digits = bd.precision();
        return digits <= 16;
    }

    /**
     * Common element number type.
     *
     * @param aa aa
     * @param bb bb
     * @return common element number type result
     */
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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
                switch (bt) {
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

    /**
     * Number type.
     *
     * @param aa aa
     * @return number type result
     */
    public static Class<?> numberType(Class<? extends Number> aa) {
        if (aa == null) {
            return Number.class;
        }
        if (NBigComplex.class.isAssignableFrom(aa)) {
            return NBigComplex.class;
        }
        if (BigDecimal.class.isAssignableFrom(aa)) {
            return NBigComplex.class;
        }
        if (BigInteger.class.isAssignableFrom(aa)) {
            return NBigComplex.class;
        }
        if (NDoubleComplex.class.isAssignableFrom(aa)) {
            return NDoubleComplex.class;
        }
        if (NFloatComplex.class.isAssignableFrom(aa)) {
            return NFloatComplex.class;
        }
        switch (aa.getName()) {
            case "byte":
            case "java.lang.Byte":
                return Byte.class;
            case "short":
            case "java.lang.Short":
                return Short.class;
            case "int":
            case "java.lang.Integer":
                return Integer.class;
            case "long":
            case "java.lang.Long":
                return Long.class;
            case "float":
            case "java.lang.Float":
                return Float.class;
            case "double":
            case "java.lang.Double":
                return Double.class;
        }
        return aa;
    }

    /**
     * Common number type.
     *
     * @param aa aa
     * @param bb bb
     * @return common number type result
     */
    public static Class<?> commonNumberType(Class<? extends Number> aa, Class<? extends Number> bb) {
        if (aa == null && bb == null) {
            return Number.class;
        }
        if (aa == null) {
            return bb;
        }
        if (bb == null) {
            return aa;
        }
        if (NBigComplex.class.isAssignableFrom(aa) || NBigComplex.class.isAssignableFrom(bb)) {
            return NBigComplex.class;
        }
        if (NDoubleComplex.class.isAssignableFrom(aa) || NDoubleComplex.class.isAssignableFrom(bb)) {
            if (BigInteger.class.isAssignableFrom(aa) || BigInteger.class.isAssignableFrom(bb)) {
                return NBigComplex.class;
            }
            return NDoubleComplex.class;
        }
        if (NFloatComplex.class.isAssignableFrom(aa) || NFloatComplex.class.isAssignableFrom(bb)) {
            if (BigInteger.class.isAssignableFrom(aa) || BigInteger.class.isAssignableFrom(bb)) {
                return NBigComplex.class;
            }
            if (Long.class.equals(aa) || Long.class.equals(bb)) {
                return NDoubleComplex.class;
            }
            return NFloatComplex.class;
        }
        if (BigDecimal.class.isAssignableFrom(aa) || BigDecimal.class.isAssignableFrom(bb)) {
            return BigDecimal.class;
        }
        if (BigInteger.class.isAssignableFrom(aa) || BigInteger.class.isAssignableFrom(bb)) {
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

    /**
     * Adds the specified numbers.
     *
     * @param a a
     * @param b b
     * @return add numbers result
     */
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
        Class<?> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asByte().get() n literal.of(b).as byte().get()
               */
                return (byte) (NLiteral.of(a).asByte().get() + NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asShort().get() n literal.of(b).as short().get()
               */
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
                return NLiteral.of(a).asFloatComplex().get().addFloatComplex(NLiteral.of(b).asFloatComplex().get()).numberValue();
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().addDoubleComplex(NLiteral.of(b).asDoubleComplex().get()).numberValue();
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().addBigComplex(NLiteral.of(b).asBigComplex().get()).numberValue();
            }
        }

        /**
         * N illegal argument exception.
         *
         * @param ct) ct)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    /**
     * Substruct numbers.
     *
     * @param a a
     * @param b b
     * @return substruct numbers result
     */
    public static Number substructNumbers(Number a, Number b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            /**
             * Negate number.
             *
             * @param b b
             * @return negate number result
             */
            return negateNumber(b);
        }
        if (b == null) {
            return a;
        }
        Class<?> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asByte().get() n literal.of(b).as byte().get()
               */
                return (byte) (NLiteral.of(a).asByte().get() - NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asShort().get() n literal.of(b).as short().get()
               */
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
                return NLiteral.of(a).asFloatComplex().get().subtractFloatComplex(NLiteral.of(b).asFloatComplex().get()).numberValue();
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().subtractDoubleComplex(NLiteral.of(b).asDoubleComplex().get()).numberValue();
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().subtractBigComplex(NLiteral.of(b).asBigComplex().get()).numberValue();
            }
        }
        /**
         * N illegal argument exception.
         *
         * @param ct) ct)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    /**
     * Multiply numbers.
     *
     * @param a a
     * @param b b
     * @param mc mc
     * @return multiply numbers result
     */
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
        Class<?> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asByte().get() n literal.of(b).as byte().get()
               */
                return (byte) (NLiteral.of(a).asByte().get() * NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asShort().get() n literal.of(b).as short().get()
               */
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
                return NLiteral.of(a).asFloatComplex().get().multiplyFloatComplex(NLiteral.of(b).asFloatComplex().get()).numberValue();
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().multiplyDoubleComplex(NLiteral.of(b).asDoubleComplex().get()).numberValue();
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().multiplyBigComplex(NLiteral.of(b).asBigComplex().get(), mc).numberValue();
            }
        }
        /**
         * N illegal argument exception.
         *
         * @param ct) ct)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    /**
     * Divide numbers.
     *
     * @param a a
     * @param b b
     * @param mc mc
     * @return divide numbers result
     */
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
        Class<?> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asByte().get() n literal.of(b).as byte().get()
               */
                return (byte) (NLiteral.of(a).asByte().get() / NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asShort().get() n literal.of(b).as short().get()
               */
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
                return NLiteral.of(a).asFloatComplex().get().divideFloatComplex(NLiteral.of(b).asFloatComplex().get()).numberValue();
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().divideDoubleComplex(NLiteral.of(b).asDoubleComplex().get()).numberValue();
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().divideBigComplex(NLiteral.of(b).asBigComplex().get(), mc).numberValue();
            }
        }
        /**
         * N illegal argument exception.
         *
         * @param ct) ct)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    /**
     * Reminder numbers.
     *
     * @param a a
     * @param b b
     * @return reminder numbers result
     */
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
        Class<?> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asByte().get() n literal.of(b).as byte().get()
               */
                return (byte) (NLiteral.of(a).asByte().get() % NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
              /**
               * Return.
               *
               * @param NLiteral.of(b).asShort().get() n literal.of(b).as short().get()
               */
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
        /**
         * N illegal argument exception.
         *
         * @param ct) ct)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    /**
     * Power numbers.
     *
     * @param a a
     * @param b b
     * @param mc mc
     * @return power numbers result
     */
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
        Class<?> ct = commonNumberType(a.getClass(), b.getClass());
        switch (ct.getName()) {
            case "java.lang.Byte": {
              /**
               * Return.
               *
               * @param Math.pow(NLiteral.of(a).asByte().get() math.pow(n literal.of(a).as byte().get()
               * @param NLiteral.of(b).asByte().get() n literal.of(b).as byte().get()
               */
                return (byte) Math.pow(NLiteral.of(a).asByte().get(), NLiteral.of(b).asByte().get());
            }
            case "java.lang.Short": {
              /**
               * Return.
               *
               * @param Math.pow(NLiteral.of(a).asShort().get() math.pow(n literal.of(a).as short().get()
               * @param NLiteral.of(b).asShort().get() n literal.of(b).as short().get()
               */
                return (short) Math.pow(NLiteral.of(a).asShort().get(), NLiteral.of(b).asShort().get());
            }
            case "java.lang.Integer": {
              /**
               * Return.
               *
               * @param Math.pow(NLiteral.of(a).asInt().get() math.pow(n literal.of(a).as int().get()
               * @param NLiteral.of(b).asInt().get() n literal.of(b).as int().get()
               */
                return (int) Math.pow(NLiteral.of(a).asInt().get(), NLiteral.of(b).asInt().get());
            }
            case "java.lang.Long": {
              /**
               * Return.
               *
               * @param Math.pow(NLiteral.of(a).asLong().get() math.pow(n literal.of(a).as long().get()
               * @param NLiteral.of(b).asLong().get() n literal.of(b).as long().get()
               */
                return (long) Math.pow(NLiteral.of(a).asLong().get(), NLiteral.of(b).asLong().get());
            }
            case "java.lang.Float": {
              /**
               * Return.
               *
               * @param Math.pow(NLiteral.of(a).asFloat().get() math.pow(n literal.of(a).as float().get()
               * @param NLiteral.of(b).asFloat().get() n literal.of(b).as float().get()
               */
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
        /**
         * N illegal argument exception.
         *
         * @param ct) ct)
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", ct));
    }

    /**
     * Negate number.
     *
     * @param a a
     * @return negate number result
     */
    public static Number negateNumber(Number a) {
        if (a == null) {
            return null;
        }
        switch (a.getClass().getName()) {
            case "java.lang.Byte": {
              /**
               * Return.
               *
               * @param (-NLiteral.of(a).asByte().get() (-n literal.of(a).as byte().get()
               */
                return (byte) (-NLiteral.of(a).asByte().get());
            }
            case "java.lang.Short": {
              /**
               * Return.
               *
               * @param (-NLiteral.of(a).asShort().get() (-n literal.of(a).as short().get()
               */
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
                return NLiteral.of(a).asFloatComplex().get().negateFloatComplex().numberValue();
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().negateDoubleComplex().numberValue();
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().negateBigComplex().numberValue();
            }
        }
        /**
         * N illegal argument exception.
         *
         * @param a.getClass()) a.get class())
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", a.getClass()));
    }

    /**
     * Inv number.
     *
     * @param a a
     * @param mc mc
     * @return inv number result
     */
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
                mc = contextMathContext(mc);
                return BigDecimal.ONE.divide(NLiteral.of(a).asBigDecimal().get(), mc);
            }
            case "java.math.BigDecimal": {
                mc = contextMathContext(mc);
                return BigDecimal.ONE.divide(NLiteral.of(a).asBigDecimal().get(), mc);
            }
            case "net.thevpc.nuts.math.NFloatComplex": {
                return NLiteral.of(a).asFloatComplex().get().invFloatComplex().numberValue();
            }
            case "net.thevpc.nuts.math.NDoubleComplex": {
                return NLiteral.of(a).asDoubleComplex().get().invDoubleComplex().numberValue();
            }
            case "net.thevpc.nuts.math.NBigComplex": {
                return NLiteral.of(a).asBigComplex().get().invBigComplex(mc).numberValue();
            }
        }
        /**
         * N illegal argument exception.
         *
         * @param a.getClass()) a.get class())
         * @return n illegal argument exception result
         */
        throw new NIllegalArgumentException(NMsg.ofC("unsupported number type", a.getClass()));
    }



}
