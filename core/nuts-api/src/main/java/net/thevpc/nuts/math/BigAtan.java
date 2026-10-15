package net.thevpc.nuts.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Arbitrary-precision atan, atan2 and pi with BigDecimal (Java 9+ for BigDecimal.sqrt).
 *
 * Method:
 *  1. atan2(y, x) is reduced to atan(y/x) plus a quadrant correction (+/- pi).
 *  2. atan(t): use oddness and atan(t) = pi/2 - atan(1/t) to get 0 <= t <= 1, then the half-angle identity
 *       atan(t) = 2 atan( t / (1 + sqrt(1 + t^2)) )
 *     a few times until t < 0.1, then the Taylor series t - t^3/3 + t^5/5 - ... (about 1 digit per term).
 *  3. pi = 16 atan(1/5) - 4 atan(1/239)  (Machin), so no hard-coded digits are needed.
 *
 * Every public method takes a MathContext (the precision of the result); internally 10 guard digits are added.
 */
public final class BigAtan {
    private BigAtan() {
    }

    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final int GUARD = 10;

    // ------------------------------------------------------------------ public API

    public static BigDecimal atan2(BigDecimal y, BigDecimal x, MathContext mc) {
        MathContext w = work(mc);
        int sx = x.signum(), sy = y.signum();
        BigDecimal r;
        if (sx == 0) {
            if (sy == 0) throw new ArithmeticException("atan2(0, 0) is undefined");
            r = pi(w).divide(TWO, w);
            if (sy < 0) r = r.negate();
        } else {
            r = atan(y.divide(x, w), w);
            if (sx < 0) {
                BigDecimal pi = pi(w);
                r = (sy >= 0) ? r.add(pi, w) : r.subtract(pi, w);   // sy == 0, x < 0 gives +pi (like Math.atan2)
            }
        }
        return r.round(mc);
    }

    public static BigDecimal atan(BigDecimal t, MathContext mc) {
        return atanInternal(t, work(mc)).round(mc);
    }

    public static BigDecimal pi(MathContext mc) {
        MathContext w = work(mc);
        BigDecimal a = atanSeries(BigDecimal.ONE.divide(BigDecimal.valueOf(5), w), w);
        BigDecimal b = atanSeries(BigDecimal.ONE.divide(BigDecimal.valueOf(239), w), w);
        return a.multiply(BigDecimal.valueOf(16)).subtract(b.multiply(BigDecimal.valueOf(4)), w).round(mc);
    }

    // ------------------------------------------------------------------ internals

    private static MathContext work(MathContext mc) {
        return new MathContext(mc.getPrecision() + GUARD, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal atanInternal(BigDecimal t, MathContext w) {
        if (t.signum() == 0) return BigDecimal.ZERO;
        if (t.signum() < 0) return atanInternal(t.negate(), w).negate();
        if (t.compareTo(BigDecimal.ONE) > 0) {                       // atan(t) = pi/2 - atan(1/t)
            BigDecimal inv = BigDecimal.ONE.divide(t, w);
            return pi(w).divide(TWO, w).subtract(atanInternal(inv, w), w);
        }
        // 0 < t <= 1: halve the angle until t is small, then the series
        BigDecimal limit = new BigDecimal("0.1");
        int halvings = 0;
        while (t.compareTo(limit) > 0) {
            BigDecimal root = sqrt(BigDecimal.ONE.add(t.multiply(t, w)), w);
            t = t.divide(BigDecimal.ONE.add(root), w);
            halvings++;
        }
        BigDecimal r = atanSeries(t, w);
        return r.multiply(BigDecimal.valueOf(2).pow(halvings), w);
    }

    /** Square root by Newton iteration (BigDecimal.sqrt only exists since Java 9). */
    public static BigDecimal sqrt(BigDecimal x, MathContext mc) {
        if (x.signum() < 0) throw new ArithmeticException("sqrt of a negative number");
        if (x.signum() == 0) return BigDecimal.ZERO;
        MathContext w = work(mc);
        // x = f * 10^e with f in [0.1, 1); make the exponent even so the initial guess is a plain double sqrt
        int e = x.precision() - x.scale();
        int e2 = e - Math.floorMod(e, 2);
        BigDecimal scaled = x.movePointLeft(e2);                          // in [0.1, 10)
        BigDecimal r = new BigDecimal(Math.sqrt(scaled.doubleValue())).movePointRight(e2 / 2);
        BigDecimal eps = BigDecimal.ONE.movePointLeft(w.getPrecision());
        for (int i = 0; i < 100; i++) {
            BigDecimal next = r.add(x.divide(r, w), w).divide(TWO, w);
            boolean done = next.subtract(r).abs().compareTo(next.multiply(eps, w)) <= 0;
            r = next;
            if (done) break;
        }
        return r.round(mc);
    }

    /** Taylor series of atan for small |t| (|t| <= 0.2 converges in ~precision terms). */
    private static BigDecimal atanSeries(BigDecimal t, MathContext w) {
        BigDecimal t2 = t.multiply(t, w);
        BigDecimal power = t;                 // t^(2k+1)
        BigDecimal sum = t;
        BigDecimal eps = BigDecimal.ONE.movePointLeft(w.getPrecision() + 2);
        for (int k = 1; k < 100_000; k++) {
            power = power.multiply(t2, w);
            BigDecimal term = power.divide(BigDecimal.valueOf(2L * k + 1), w);
            if (term.abs().compareTo(eps) < 0) break;
            sum = (k % 2 == 1) ? sum.subtract(term, w) : sum.add(term, w);
        }
        return sum;
    }

}