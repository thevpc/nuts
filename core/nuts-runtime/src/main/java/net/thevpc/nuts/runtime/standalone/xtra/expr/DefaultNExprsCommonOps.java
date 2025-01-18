package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprCommonOp;
import net.thevpc.nuts.expr.NExprOpType;
import net.thevpc.nuts.util.NPlatformSignature;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asByte().get().byteValue() == NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asShort().get().shortValue() == NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asInt().get().intValue() == NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asLong().get().longValue() == NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigInt().get().equals(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() == NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asDouble().get().doubleValue() == NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigDecimal().get().equals(NLiteral.of(b).asBigDecimal().get());
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
        declare2(NExprCommonOp.EQ, NExprOpType.INFIX, NPlatformSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return Objects.equals(a, b);
                    }
                }
        );
    }

    private void declareNe() {
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return NLiteral.of(a).asByte().get().byteValue() != NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return NLiteral.of(a).asShort().get().shortValue() != NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return NLiteral.of(a).asInt().get().intValue() != NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return NLiteral.of(a).asLong().get().longValue() != NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return !NLiteral.of(a).asBigInt().get().equals(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() != NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return NLiteral.of(a).asDouble().get().doubleValue() != NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return !NLiteral.of(a).asBigDecimal().get().equals(NLiteral.of(b).asBigDecimal().get());
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
        declare2(NExprCommonOp.NE, NExprOpType.INFIX, NPlatformSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return false;
                        }
                        if (a == null || b == null) {
                            return true;
                        }
                        return !Objects.equals(a,b);
                    }
                }
        );
    }

    private void declareGt() {
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asByte().get().byteValue() > NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asShort().get().shortValue() > NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asInt().get().intValue() > NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asLong().get().longValue() > NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigInt().get().compareTo(NLiteral.of(b).asBigInt().get()) > 0;
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() > NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asDouble().get().doubleValue() > NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.GT, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigDecimal().get().compareTo(NLiteral.of(b).asBigDecimal().get()) > 0;
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declareGte() {
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asByte().get().byteValue() >= NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asShort().get().shortValue() >= NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asInt().get().intValue() >= NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asLong().get().longValue() >= NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigInt().get().compareTo(NLiteral.of(b).asBigInt().get()) >= 0;
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() >= NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asDouble().get().doubleValue() >= NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.GTE, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigDecimal().get().compareTo(NLiteral.of(b).asBigDecimal().get()) >= 0;
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declareLt() {
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asByte().get().byteValue() < NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asShort().get().shortValue() < NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asInt().get().intValue() < NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asLong().get().longValue() < NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigInt().get().compareTo(NLiteral.of(b).asBigInt().get()) < 0;
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() < NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asDouble().get().doubleValue() < NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.LT, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigDecimal().get().compareTo(NLiteral.of(b).asBigDecimal().get()) < 0;
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declareLte() {
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asByte().get().byteValue() <= NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asShort().get().shortValue() <= NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asInt().get().intValue() <= NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asLong().get().longValue() <= NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigInt().get().compareTo(NLiteral.of(b).asBigInt().get()) <= 0;
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() <= NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asDouble().get().doubleValue() <= NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.LTE, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Boolean>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return true;
                        }
                        if (a == null || b == null) {
                            return false;
                        }
                        return NLiteral.of(a).asBigDecimal().get().compareTo(NLiteral.of(b).asBigDecimal().get()) <= 0;
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declarePlus() {
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asByte().get().byteValue() + NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asShort().get().shortValue() + NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asInt().get().intValue() + NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asLong().get().longValue() + NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigInt().get().add(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asFloat().get().floatValue() + NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asDouble().get().doubleValue() + NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigDecimal().get().add(NLiteral.of(b).asBigDecimal().get());
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
        declare2(NExprCommonOp.PLUS, NExprOpType.INFIX, NPlatformSignature.of(Object.class, Object.class),
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
                        return String.valueOf(a)+String.valueOf(b);
                    }
                }
        );
    }

    private void declareMinus() {
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asByte().get().byteValue() - NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asShort().get().shortValue() - NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asInt().get().intValue() - NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asLong().get().longValue() - NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigInt().get().subtract(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asFloat().get().floatValue() - NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asDouble().get().doubleValue() - NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.MINUS, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigDecimal().get().subtract(NLiteral.of(b).asBigDecimal().get());
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declareMul() {
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asByte().get().byteValue() * NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asShort().get().shortValue() * NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asInt().get().intValue() * NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asLong().get().longValue() * NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigInt().get().multiply(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asFloat().get().floatValue() * NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asDouble().get().doubleValue() * NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.MUL, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigDecimal().get().multiply(NLiteral.of(b).asBigDecimal().get());
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declarePow() {
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return powerLong(NLiteral.of(a).asByte().get().byteValue(), NLiteral.of(b).asByte().get().byteValue());
                    }
                }
        );
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return powerLong(NLiteral.of(a).asShort().get().shortValue(), NLiteral.of(b).asShort().get().shortValue());
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return powerLong(NLiteral.of(a).asInt().get().intValue(), NLiteral.of(b).asInt().get().intValue());
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return powerLong(NLiteral.of(a).asLong().get().longValue(), NLiteral.of(b).asLong().get().longValue());
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        BigInteger aa = NLiteral.of(a).asBigInt().get();
                        BigInteger bb = NLiteral.of(b).asBigInt().get();
                        return powerBigDecimal(new BigDecimal(aa), new BigDecimal(bb), new MathContext(0, RoundingMode.DOWN));
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Number>() {
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
                        return Math.pow(NLiteral.of(a).asFloat().get().floatValue(), NLiteral.of(b).asFloat().get().floatValue());
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Number>() {
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
                        return Math.pow(NLiteral.of(a).asDouble().get().doubleValue(), NLiteral.of(b).asDouble().get().doubleValue());
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.POW, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Number>() {
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
                        BigDecimal aa = NLiteral.of(a).asBigDecimal().get();
                        BigDecimal bb = NLiteral.of(b).asBigDecimal().get();
                        return powerBigDecimal(aa, bb, new MathContext(aa.precision() + bb.precision(), RoundingMode.DOWN));
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declareDiv() {
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asByte().get().byteValue() / NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asShort().get().shortValue() / NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asInt().get().intValue() / NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asLong().get().longValue() / NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigInt().get().divide(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asFloat().get().floatValue() / NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asDouble().get().doubleValue() / NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.DIV, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigDecimal().get().divide(NLiteral.of(b).asBigDecimal().get());
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declareRem() {
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asByte().get().byteValue() % NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asShort().get().shortValue() % NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asInt().get().intValue() % NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asLong().get().longValue() % NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigInt().get().remainder(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asFloat().get().floatValue() % NLiteral.of(b).asFloat().get().floatValue();
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asDouble().get().doubleValue() % NLiteral.of(b).asDouble().get().doubleValue();
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.REM, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigDecimal().get().remainder(NLiteral.of(b).asBigDecimal().get());
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declarePlusPrefix() {
        declare1(NExprCommonOp.PLUS, NExprOpType.PREFIX, NPlatformSignature.of(Object.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        return a;
                    }
                }
        );
    }

    private void declareMinusPrefix() {
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Byte.class),
                new NFunction<Object, Number>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return -NLiteral.of(a).asByte().get();
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Short.class),
                new NFunction<Object, Number>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return -NLiteral.of(a).asShort().get().shortValue();
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Integer.class),
                new NFunction<Object, Number>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return -NLiteral.of(a).asInt().get().intValue();
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Long.class),
                new NFunction<Object, Number>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return -NLiteral.of(a).asLong().get().longValue();
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(BigInteger.class),
                new NFunction<Object, Number>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return NLiteral.of(a).asBigInt().get().negate();
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Float.class),
                new NFunction<Object, Number>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return -NLiteral.of(a).asFloat().get().floatValue();
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Double.class),
                new NFunction<Object, Number>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return -NLiteral.of(a).asDouble().get();
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(BigDecimal.class),
                new NFunction<Object, Number>() {
                    @Override
                    public Number apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return NLiteral.of(a).asBigDecimal().get().negate();
                    }
                }
        );
    }

    private void declareNot() {
        declare1(NExprCommonOp.NOT, NExprOpType.PREFIX, NPlatformSignature.of(Byte.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return true;
                        }
                        return NLiteral.of(a).asByte().get() == 0;
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Short.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return true;
                        }
                        return NLiteral.of(a).asShort().get().shortValue() != 0;
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Integer.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return true;
                        }
                        return NLiteral.of(a).asInt().get().intValue() == 0;
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Long.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return true;
                        }
                        return NLiteral.of(a).asLong().get().longValue() == 0;
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(BigInteger.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return null;
                        }
                        return NLiteral.of(a).asBigInt().get().equals(BigInteger.ZERO);
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Float.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return true;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() == 0;
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(Double.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return true;
                        }
                        return NLiteral.of(a).asDouble().get() == 0;
                    }
                }
        );
        declare1(NExprCommonOp.MINUS, NExprOpType.PREFIX, NPlatformSignature.of(BigDecimal.class),
                new NFunction<Object, Object>() {
                    @Override
                    public Object apply(Object a) {
                        if (a == null) {
                            return true;
                        }
                        return NLiteral.of(a).asBigDecimal().get().equals(BigDecimal.ZERO);
                    }
                }
        );
    }

    private void declareAnd() {
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Boolean.class, Boolean.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Boolean) b;
                        }
                        if (b == null) {
                            return (Boolean) a;
                        }
                        return NLiteral.of(a).asBoolean().get().booleanValue() && NLiteral.of(b).asBoolean().get().booleanValue();
                    }
                }
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            if (b instanceof Boolean) {
                                return (Boolean) b;
                            }
                            return NBlankable.isBlank(b);
                        }
                        if (b == null) {
                            if (a instanceof Boolean) {
                                return (Boolean) a;
                            }
                            return NBlankable.isBlank(a);
                        }
                        return NBlankable.isBlank(a) && NBlankable.isBlank(b);
                    }
                }
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asByte().get().byteValue() & NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asShort().get().shortValue() & NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asInt().get().intValue() & NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asLong().get().longValue() & NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigInt().get().and(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() != 0 && NLiteral.of(b).asFloat().get().floatValue() != 0;
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        return NLiteral.of(a).asDouble().get().doubleValue() == 0 & NLiteral.of(b).asDouble().get().doubleValue() == 0;
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.AND, NExprCommonOp.AND_BITS}, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
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
                        return
                                NLiteral.of(a).asBigDecimal().get().equals(BigDecimal.ZERO)
                                        && NLiteral.of(b).asBigDecimal().get().equals(BigDecimal.ZERO);
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declareOr() {
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Boolean.class, Boolean.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Boolean) b;
                        }
                        if (b == null) {
                            return (Boolean) a;
                        }
                        return NLiteral.of(a).asBoolean().get().booleanValue() || NLiteral.of(b).asBoolean().get().booleanValue();
                    }
                }
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            if (b instanceof Boolean) {
                                return (Boolean) b;
                            }
                            return NBlankable.isBlank(b);
                        }
                        if (b == null) {
                            if (a instanceof Boolean) {
                                return (Boolean) a;
                            }
                            return NBlankable.isBlank(a);
                        }
                        return NBlankable.isBlank(a) || NBlankable.isBlank(b);
                    }
                }
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asByte().get().byteValue() | NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asShort().get().shortValue() | NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asInt().get().intValue() | NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asLong().get().longValue() | NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigInt().get().or(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        return NLiteral.of(a).asFloat().get().floatValue() != 0 | NLiteral.of(b).asFloat().get().floatValue() != 0;
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        return NLiteral.of(a).asDouble().get().doubleValue() == 0 | NLiteral.of(b).asDouble().get().doubleValue() == 0;
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(new NExprCommonOp[]{NExprCommonOp.OR, NExprCommonOp.OR_BITS}, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
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
                        return
                                NLiteral.of(a).asBigDecimal().get().equals(BigDecimal.ZERO)
                                        | NLiteral.of(b).asBigDecimal().get().equals(BigDecimal.ZERO);
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }


    private void declareXOr() {
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(Boolean.class, Boolean.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Boolean) b;
                        }
                        if (b == null) {
                            return (Boolean) a;
                        }
                        return NLiteral.of(a).asBoolean().get().booleanValue() ^ NLiteral.of(b).asBoolean().get().booleanValue();
                    }
                }
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(Object.class, Object.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Boolean apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            if (b instanceof Boolean) {
                                return (Boolean) b;
                            }
                            return NBlankable.isBlank(b);
                        }
                        if (b == null) {
                            if (a instanceof Boolean) {
                                return (Boolean) a;
                            }
                            return NBlankable.isBlank(a);
                        }
                        return NBlankable.isBlank(a) ^ NBlankable.isBlank(b);
                    }
                }
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(Byte.class, Byte.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asByte().get().byteValue() ^ NLiteral.of(b).asByte().get().byteValue();
                    }
                }
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(Short.class, Short.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asShort().get().shortValue() ^ NLiteral.of(b).asShort().get().shortValue();
                    }
                },
                NPlatformSignature.of(Short.class, Byte.class), NPlatformSignature.of(Byte.class, Short.class)
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(Integer.class, Integer.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asInt().get().intValue() ^ NLiteral.of(b).asInt().get().intValue();
                    }
                },
                NPlatformSignature.of(Integer.class, Byte.class), NPlatformSignature.of(Byte.class, Integer.class)
                , NPlatformSignature.of(Integer.class, Short.class), NPlatformSignature.of(Short.class, Integer.class)
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(Long.class, Long.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asLong().get().longValue() ^ NLiteral.of(b).asLong().get().longValue();
                    }
                },
                NPlatformSignature.of(Long.class, Byte.class), NPlatformSignature.of(Byte.class, Long.class)
                , NPlatformSignature.of(Long.class, Short.class), NPlatformSignature.of(Short.class, Long.class)
                , NPlatformSignature.of(Long.class, Integer.class), NPlatformSignature.of(Integer.class, Long.class)
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(BigInteger.class, BigInteger.class),
                new NFunction2<Object, Object, Number>() {
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
                        return NLiteral.of(a).asBigInt().get().xor(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigInteger.class, Byte.class), NPlatformSignature.of(Byte.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Short.class), NPlatformSignature.of(Short.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Integer.class), NPlatformSignature.of(Integer.class, BigInteger.class)
                , NPlatformSignature.of(BigInteger.class, Long.class), NPlatformSignature.of(Long.class, BigInteger.class)
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(Float.class, Float.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        float v1 = NLiteral.of(a).asFloat().get().floatValue();
                        float v2 = NLiteral.of(b).asFloat().get().floatValue();
                        return Float.intBitsToFloat(Float.floatToIntBits(v1) ^ Float.floatToIntBits(v2));
                    }
                },
                NPlatformSignature.of(Float.class, Byte.class), NPlatformSignature.of(Byte.class, Float.class)
                , NPlatformSignature.of(Float.class, Short.class), NPlatformSignature.of(Short.class, Float.class)
                , NPlatformSignature.of(Float.class, Integer.class), NPlatformSignature.of(Integer.class, Float.class)
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(Double.class, Double.class),
                new NFunction2<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) {
                        if (a == null && b == null) {
                            return null;
                        }
                        if (a == null) {
                            return (Number) b;
                        }
                        if (b == null) {
                            return (Number) a;
                        }
                        double v1 = NLiteral.of(a).asDouble().get().doubleValue();
                        double v2 = NLiteral.of(b).asDouble().get().doubleValue();
                        return Double.longBitsToDouble(Double.doubleToLongBits(v1) ^ Double.doubleToLongBits(v2));
                    }
                },
                NPlatformSignature.of(Double.class, Byte.class), NPlatformSignature.of(Byte.class, Double.class)
                , NPlatformSignature.of(Double.class, Short.class), NPlatformSignature.of(Short.class, Double.class)
                , NPlatformSignature.of(Double.class, Integer.class), NPlatformSignature.of(Integer.class, Double.class)
                , NPlatformSignature.of(Double.class, Long.class), NPlatformSignature.of(Long.class, Double.class)
                , NPlatformSignature.of(Float.class, Long.class), NPlatformSignature.of(Long.class, Float.class)
        );
        declare2(NExprCommonOp.XOR, NExprOpType.INFIX, NPlatformSignature.of(BigDecimal.class, BigDecimal.class),
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
                        return
                                NLiteral.of(a).asBigInt().get().xor(NLiteral.of(b).asBigInt().get());
                    }
                },
                NPlatformSignature.of(BigDecimal.class, Byte.class), NPlatformSignature.of(Byte.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Short.class), NPlatformSignature.of(Short.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Integer.class), NPlatformSignature.of(Integer.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Long.class), NPlatformSignature.of(Long.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Float.class), NPlatformSignature.of(Float.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, Double.class), NPlatformSignature.of(Double.class, BigDecimal.class)
                , NPlatformSignature.of(BigDecimal.class, BigInteger.class), NPlatformSignature.of(Double.class, BigInteger.class)
        );
    }

    private void declare2(NExprCommonOp[] op, NExprOpType type, NPlatformSignature sig, NFunction2<?, ?, ?> value, NPlatformSignature... sigs) {
        for (NExprCommonOp o : op) {
            declare2(o, type, sig, value, sigs);
        }
    }

    private void declare2(NExprCommonOp op, NExprOpType type, NPlatformSignature sig, NFunction2<?, ?, ?> value, NPlatformSignature... sigs) {
        NPlatformSignatureMap<Object> sigMap = commonOps.computeIfAbsent(new NExprCommonOpAndType(op, type), r -> new NPlatformSignatureMap<>(Object.class));
        sigMap.putMulti(sig, value, sigs);
    }

    private void declare1(NExprCommonOp op, NExprOpType type, NPlatformSignature sig, NFunction<?, ?> value, NPlatformSignature... sigs) {
        NPlatformSignatureMap<Object> sigMap = commonOps.computeIfAbsent(new NExprCommonOpAndType(op, type), r -> new NPlatformSignatureMap<>(Object.class));
        sigMap.putMulti(sig, value, sigs);
    }

    public NOptional<NFunction2<?, ?, ?>> findFunction2(NExprCommonOp op, NExprOpType type, NPlatformSignature sig) {
        NAssert.requireTrue(sig.size() == 2, "sig size");
        NPlatformSignatureMap<Object> sm = commonOps.get(new NExprCommonOpAndType(op, type));
        if (sm != null) {
            NOptional<Object> v = sm.get(sig);
            if (v.isPresent() && v.get() instanceof NFunction2) {
                return (NOptional) v;
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("%s %s %s", op.image() + type.id(), sig));
    }

    public NOptional<NFunction<?, ?>> findFunction1(NExprCommonOp op, NExprOpType type, NPlatformSignature sig) {
        NAssert.requireTrue(sig.size() == 1, "sig size");
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
