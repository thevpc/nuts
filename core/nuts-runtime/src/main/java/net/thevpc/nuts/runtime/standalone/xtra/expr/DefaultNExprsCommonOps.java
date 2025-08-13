package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.expr.NExprCommonOp;
import net.thevpc.nuts.expr.NExprOpType;
import net.thevpc.nuts.util.NPlatformArgsSignature;
import net.thevpc.nuts.util.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultNExprsCommonOps {
    private final Map<NExprCommonOpAndType, NPlatformSignatureMap<Object>> commonOps = new HashMap<>();

    public DefaultNExprsCommonOps() {
        declareEq();
        declareNe();
        declareGt();
        declareGte();
        declareLt();
        declareLte();
        declarePlus();
        declareMinus();
        declareMul();
        declareDiv();
        declareRem();
        declarePow();
        declarePlusPrefix();
        declareMinusPrefix();
        declareNot();
        declareAnd();
        declareOr();
        declareXOr();
    }

    private void declareEq() {
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.eqNumbers((Number) a, (Number) b);
                        }
                        return Objects.equals(a, b);
                    }
                }
        );
    }

    private void declareNe() {
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return !NNumberUtils.eqNumbers((Number) a, (Number) b);
                        }
                        return !Objects.equals(a, b);
                    }
                }
        );
    }

    private void declareGt() {
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        return NUtils.compareObjects(a, b) > 0;
                    }
                }
        );
    }

    private void declareGte() {
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        return NUtils.compareObjects(a, b) >= 0;
                    }
                }
        );
    }

    private void declareLt() {
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        return NUtils.compareObjects(a, b) < 0;
                    }
                }
        );
    }


    private void declareLte() {
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        return NUtils.compareObjects(a, b) <= 0;
                    }
                }
        );
    }

    private void declarePlus() {
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return b;
                        }
                        if (b == null) {
                            return a;
                        }
                        NLiteral aa = NLiteral.of(a);
                        NLiteral bb = NLiteral.of(b);
                        if (aa.isNumber() && bb.isNumber()) {
                            return NNumberUtils.addNumbers(aa.asNumber().get(), bb.asNumber().get());
                        }
                        if (aa.isString() || bb.isString()) {
                            return String.valueOf(a) + String.valueOf(b);
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '+' operator for %s %s", a, b));
                    }
                }
        );
    }

    private void declareMinus() {
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            if (b instanceof Number) {
                                return NNumberUtils.negateNumber((Number) b);
                            }
                            return b;
                        }
                        if (b == null) {
                            return a;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.substructNumbers((Number) a, (Number) b);
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '-' operator for %s %s", a, b));
                    }
                }
        );
    }

    private void declareMul() {
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            if (b instanceof Number) {
                                return NNumberUtils.negateNumber((Number) b);
                            }
                            return b;
                        }
                        if (b == null) {
                            return a;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.multiplyNumbers((Number) a, (Number) b, null);
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '-' operator for %s %s", a, b));
                    }
                }
        );
    }

    private void declarePow() {
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Number apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.powNumbers((Number) a, (Number) b, null);
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate 'pow' operator for %s %s", a, b));
                    }
                }
        );
    }

    private void declareDiv() {
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Number apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.divideNumbers((Number) a, (Number) b, null);
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '/' operator for %s %s", a, b));
                    }
                }
        );
    }

    private void declareRem() {
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Number apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.reminderNumbers((Number) a, (Number) b);
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '%' operator for %s %s", a, b));
                    }
                }
        );
    }

    private void declarePlusPrefix() {
        declare1(NExprCommonOp.PLUS, NExprOpType.PREFIX, NPlatformArgsSignature.of(Object.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        return a;
                    }
                }
        );
    }

    private void declareMinusPrefix() {
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformArgsSignature.of(Object.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        if (a instanceof Number) {
                            return NNumberUtils.negateNumber((Number) a);
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '-' operator for %s", a));
                    }
                }
        );
    }

    private void declareNot() {
        declare1(NExprCommonOp.NOT, NExprOpType.PREFIX, NPlatformArgsSignature.of(Object.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return true;
                        }
                        Boolean u = NLiteral.of(a).asBoolean().orNull();
                        if (u != null) {
                            return !u;
                        }
                        Number n = NLiteral.of(a).asNumber().orNull();
                        if (n != null) {
                            return NNumberUtils.eqNumbers(n, 0);
                        }
                        if (a instanceof CharSequence) {
                            return ((CharSequence) a).length() == 0;
                        }
                        return a != null;
                    }
                }
        );
    }

    private void declareAnd() {
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return b;
                        }
                        if (b == null) {
                            return a;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.andNumbers((Number) a, (Number) b);
                        }
                        if (a instanceof Boolean && b instanceof Boolean) {
                            return (Boolean) a & (Boolean) b;
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '&' operator for %s %s", a, b));
                    }
                }
        );
    }

    private void declareOr() {
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return b;
                        }
                        if (b == null) {
                            return a;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.orNumbers((Number) a, (Number) b);
                        }
                        if (a instanceof Boolean && b instanceof Boolean) {
                            return (Boolean) a | (Boolean) b;
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '|' operator for %s %s", a, b));
                    }
                }
        );
    }


    private void declareXOr() {
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformArgsSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return b;
                        }
                        if (b == null) {
                            return a;
                        }
                        if (a instanceof Number && b instanceof Number) {
                            return NNumberUtils.xorNumbers((Number) a, (Number) b);
                        }
                        if (a instanceof Boolean && b instanceof Boolean) {
                            return (Boolean) a ^ (Boolean) b;
                        }
                        throw new NIllegalArgumentException(NMsg.ofC("unable to operate '^' operator for %s %s", a, b));
                    }
                }
        );
    }

    private void declare2(NExprCommonOp[] op, NExprOpType type, NPlatformArgsSignature sig, NFunction2<?, ?, ?> value, NPlatformArgsSignature... sigs) {
        for (NExprCommonOp o : op) {
            declare2(o, type, sig, value, sigs);
        }
    }

    private void declare2(NExprCommonOp op, NExprOpType type, NPlatformArgsSignature sig, NFunction2<?, ?, ?> value, NPlatformArgsSignature... sigs) {
        NPlatformSignatureMap<Object> sigMap = commonOps.computeIfAbsent(new NExprCommonOpAndType(op, type), r -> new NPlatformSignatureMap<>(Object.class));
        sigMap.putMulti(sig, value, sigs);
    }

    private void declare1(NExprCommonOp op, NExprOpType type, NPlatformArgsSignature sig, NFunction<?, ?> value, NPlatformArgsSignature... sigs) {
        NPlatformSignatureMap<Object> sigMap = commonOps.computeIfAbsent(new NExprCommonOpAndType(op, type), r -> new NPlatformSignatureMap<>(Object.class));
        sigMap.putMulti(sig, value, sigs);
    }

    public NOptional<NFunction2<?, ?, ?>> findFunction2(NExprCommonOp op, NExprOpType type, NPlatformArgsSignature sig) {
        NAssert.requireTrue(sig.size() == 2, "sig size");
        if (sig.getType(0) == null || sig.getType(1) == null) {
            List<Object> acceptable = commonOps.entrySet().stream().filter(x -> x.getKey().getType() == type && x.getKey().getOp() == op)
                    .flatMap(x -> x.getValue().toMap().entrySet().stream().filter(y -> y.getKey().matches(sig)))
                    .collect(Collectors.toList());
            if (acceptable.size() == 1) {
                return NOptional.of((NFunction2) acceptable.get(0));
            }
            if (acceptable.size() == 0) {
                return NOptional.ofNamedEmpty(sig.toString());
            }
            return NOptional.of((NFunction2) acceptable.get(0));
        }

        NPlatformSignatureMap<Object> sm = commonOps.get(new NExprCommonOpAndType(op, type));
        if (sm != null) {
            NOptional<Object> v = sm.get(sig);
            if (v.isPresent() && v.get() instanceof NFunction2) {
                return (NOptional) v;
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s %s %s", op.image() + type.id(), sig));
    }

    public NOptional<NFunction<?, ?>> findFunction1(NExprCommonOp op, NExprOpType type, NPlatformArgsSignature sig) {
        NAssert.requireTrue(sig.size() == 1, "sig size");
        Type t = sig.getType(0);
        if (t == null) {
            List<Object> acceptable = commonOps.entrySet().stream().filter(x -> x.getKey().getType() == type && x.getKey().getOp() == op)
                    .flatMap(x -> x.getValue().toMap().values().stream())
                    .collect(Collectors.toList());
            if (acceptable.size() == 1) {
                return NOptional.of((NFunction) acceptable.get(0));
            }
            if (acceptable.size() == 0) {
                return NOptional.ofNamedEmpty(sig.toString());
            }
            return NOptional.of((NFunction) acceptable.get(0));
        }
        NPlatformSignatureMap<Object> sm = commonOps.get(new NExprCommonOpAndType(op, type));
        if (sm != null) {
            NOptional<Object> v = sm.get(sig);
            if (v.isPresent() && v.get() instanceof NFunction) {
                return (NOptional) v;
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s %s %s", op.image() + type.id(), sig));
    }


    private static Number powerBigDecimal(BigDecimal a, BigDecimal b, MathContext mc) {
        Integer ii = NLiteral.of(b).asInt().orNull();
        if (ii != null) {
            return a.pow(ii);
        }
        return power(a, b, mc);
    }

    private static Number powerLong(long a, long b) {
        if (b <= 0) {
            return Math.pow(a, b);
        }
        if (a < 0) {
            a = -a;
            if ((b & 1) == 1) {
                long re = 1;
                while (b > 0) {
                    if ((b & 1) == 1) {
                        re *= a;
                    }
                    b >>= 1;
                    a *= a;
                }
                return -re;
            }
        }
        long re = 1;
        while (b > 0) {
            if ((b & 1) == 1) {
                re *= a;
            }
            b >>= 1;
            a *= a;
        }
        return re;
    }

    public static BigDecimal naturalLog(BigDecimal value, MathContext mc) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException("Logarithm only defined for positive numbers.");
        }

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = value.subtract(BigDecimal.ONE);
        BigDecimal numerator = term;
        BigDecimal denominator = BigDecimal.ONE;

        int iterations = mc.getPrecision();
        for (int i = 0; i < iterations; i++) {
            BigDecimal fraction = numerator.divide(denominator, mc);
            if (i % 2 == 0) {
                result = result.add(fraction);
            } else {
                result = result.subtract(fraction);
            }
            numerator = numerator.multiply(term, mc);
            denominator = denominator.add(BigDecimal.ONE, mc);
        }

        return result;
    }

    // Method to calculate e^x for a BigDecimal
    public static BigDecimal exp(BigDecimal value, MathContext mc) {
        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal factorial = BigDecimal.ONE;

        int iterations = mc.getPrecision();
        for (int i = 1; i <= iterations; i++) {
            term = term.multiply(value, mc);
            factorial = factorial.multiply(BigDecimal.valueOf(i), mc);
            result = result.add(term.divide(factorial, mc), mc);
        }

        return result;
    }

    // Method to calculate a^b for BigDecimal
    public static BigDecimal power(BigDecimal base, BigDecimal exponent, MathContext mc) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException("Base must be positive for non-integer exponents.");
        }
        BigDecimal lnBase = naturalLog(base, mc);
        BigDecimal exponentLnBase = lnBase.multiply(exponent, mc);
        return exp(exponentLnBase, mc);
    }

}
