package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.reflect.NReflectMethod;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.runtime.standalone.reflect.NReflectSignatureImpl;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgParam;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NExprContextBuilderImpl implements NExprContextBuilder {

    private final NExprContextAlteration alteration = new NExprContextAlteration();
    private final NExprContext parent;
    private final NExprRPI rpi;

    public NExprContextBuilderImpl(NExprRPI rpi, NExprContext parent) {
        this.rpi = rpi;
        this.parent = parent;
    }

    public boolean isAutoDeclareVariables() {
        return alteration.isAutoDeclareVariables();
    }

    @Override
    public NExprContextBuilder setAutoDeclareVariables(boolean autoDeclareVariables) {
        alteration.setAutoDeclareVariables(autoDeclareVariables);
        return this;
    }

    @Override
    public NExprContextBuilder declareMathConstants() {
        this.declareVar(NExprVar.ofConst("pi", Math.PI));
        this.declareVar(NExprVar.ofConst("PI", Math.PI));
        this.declareVar(NExprVar.ofConst("π", Math.PI));
        this.declareVar(NExprVar.ofConst("E", Math.E));
        return this;
    }

    @Override
    public NExprContextBuilder declarePhysicsConstants() {
// Already present
        this.declareVar(NExprVar.ofConst("C", 299792458.0));       // speed of light (m/s)
        this.declareVar(NExprVar.ofConst("ε0", 8.8541878128E-12));  // permittivity of free space (F/m) — fix precision
        this.declareVar(NExprVar.ofConst("μ0", 1.25663706212E-6));  // permeability of free space (H/m) — fix precision

        // Electromagnetic
        this.declareVar(NExprVar.ofConst("η0", 376.730313668));     // free-space impedance (Ω) = μ0*C
        this.declareVar(NExprVar.ofConst("e", 1.602176634E-19));   // elementary charge (C)

        // Quantum / atomic
        this.declareVar(NExprVar.ofConst("h", 6.62607015E-34));    // Planck constant (J·s)
        this.declareVar(NExprVar.ofConst("ħ", 1.054571817E-34));   // reduced Planck (J·s)
        this.declareVar(NExprVar.ofConst("kB", 1.380649E-23));      // Boltzmann constant (J/K)
        this.declareVar(NExprVar.ofConst("NA", 6.02214076E23));     // Avogadro (mol⁻¹)
        this.declareVar(NExprVar.ofConst("me", 9.1093837015E-31));  // electron mass (kg)
        this.declareVar(NExprVar.ofConst("mp", 1.67262192369E-27)); // proton mass (kg)

        // Gravity / thermodynamics
        this.declareVar(NExprVar.ofConst("G", 6.67430E-11));       // gravitational constant (m³/kg/s²)
        this.declareVar(NExprVar.ofConst("g", 9.80665));           // standard gravity (m/s²)
        this.declareVar(NExprVar.ofConst("R", 8.314462618));       // gas constant (J/mol/K)
        this.declareVar(NExprVar.ofConst("σ", 5.670374419E-8));    // Stefan–Boltzmann (W/m²/K⁴)

        return this;
    }

    public NExprContextBuilder declareBuiltins() {
        addDefaultOp(NExprCommonOp.AND, NExprOpType.INFIX, NExprOpPrecedence.AND, NOperatorAssociativity.LEFT, "&");
        addDefaultOp(NExprCommonOp.OR, NExprOpType.INFIX, NExprOpPrecedence.OR, NOperatorAssociativity.LEFT, "|");
        addDefaultOp(NExprCommonOp.LT, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.LTE, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.GT, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.GTE, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.EQ, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.LIKE, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.EQ_REGEX, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.NE, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT, "!=", "!==", "<>");
        addDefaultOp(NExprCommonOp.PLUS, NExprOpType.INFIX, NExprOpPrecedence.PLUS, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.MINUS, NExprOpType.INFIX, NExprOpPrecedence.PLUS, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.MUL, NExprOpType.INFIX, NExprOpPrecedence.MUL, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.DIV, NExprOpType.INFIX, NExprOpPrecedence.MUL, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.REM, NExprOpType.INFIX, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.XOR, NExprOpType.INFIX, NExprOpPrecedence.OR, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.POW, NExprOpType.INFIX, NExprOpPrecedence.POW, NOperatorAssociativity.LEFT);
        addDefaultOp(NExprCommonOp.DOT, NExprOpType.INFIX, NExprOpPrecedence.DOT, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                NExprNodeValue a = args.get(0);
                NExprNodeValue b = args.get(1);
                Object instance = a.eval(context).orNull();
                return runDot(instance, b, context);
            }
        });

        addDefaultOp(NExprCommonOp.MINUS, NExprOpType.PREFIX, NExprOpPrecedence.NOT, NOperatorAssociativity.RIGHT);
        addDefaultOp(NExprCommonOp.NOT, NExprOpType.PREFIX, NExprOpPrecedence.NOT, NOperatorAssociativity.RIGHT);

        addDefaultOp(NExprCommonOp.ASSIGN, NExprOpType.INFIX, NExprOpPrecedence.ASSIGN, NOperatorAssociativity.RIGHT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                NExprNode a = args.get(0);
                if (a.nodeType() == NExprNodeType.WORD) {
                    String varName = a.name();
                    NExprVar v = context.getVar(varName).get();
                    Object newValue = args.get(1).eval(context).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        for (String relOp : new String[]{"+", "-", "*", "/", "%", "^", "**"}) {
            addDefaultOp(relOp + "=", NExprOpType.INFIX, NExprOpPrecedence.ASSIGN, NOperatorAssociativity.RIGHT, new NExprCallHandler() {
                @Override
                public Object eval(NExprCallContext callContext) {
                    String name = callContext.name();
                    List<NExprNodeValue> args = callContext.args();
                    NExprContext context = callContext.context();
                    NExprNode a = args.get(0);
                    if (a.nodeType() == NExprNodeType.WORD) {
                        String varName = a.name();
                        NExprVar v = context.getVar(varName).get();
                        Object oldValue = v.get(context);
                        Object partValue = args.get(1).eval(context).get();
                        Object newValue = context.evalInfixOperator(relOp, context.bindLiteral(oldValue), context.bindLiteral(partValue)).get();
                        v.set(newValue, context);
                        return newValue;
                    }
                    throw new IllegalArgumentException("cannot assign to non valid variable " + name);
                }
            });
        }

        addDefaultOp("++", NExprOpType.PREFIX, NExprOpPrecedence.NOT, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                NExprNode a = args.get(0);
                if (a.nodeType() == NExprNodeType.WORD) {
                    String varName = a.name();
                    NExprVar v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("+", context.bindLiteral(oldValue), context.bindLiteral((byte) 1)).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp("++", NExprOpType.POSTFIX, NExprOpPrecedence.NOT, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                NExprNode a = args.get(0);
                if (a.nodeType() == NExprNodeType.WORD) {
                    String varName = a.name();
                    NExprVar v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("+", context.bindLiteral(oldValue), context.bindLiteral((byte) 1)).get();
                    v.set(newValue, context);
                    return oldValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp("--", NExprOpType.PREFIX, NExprOpPrecedence.NOT, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                NExprNode a = args.get(0);
                if (a.nodeType() == NExprNodeType.WORD) {
                    String varName = a.name();
                    NExprVar v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("-", context.bindLiteral(oldValue), context.bindLiteral((byte) 1)).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp("--", NExprOpType.POSTFIX, NExprOpPrecedence.NOT, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                NExprNode a = args.get(0);
                if (a.nodeType() == NExprNodeType.WORD) {
                    String varName = a.name();
                    NExprVar v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("-", context.bindLiteral(oldValue), context.bindLiteral((byte) 1)).get();
                    v.set(newValue, context);
                    return oldValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp(";", NExprOpType.INFIX, NExprOpPrecedence.STATEMENT_SEPARATOR, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                Object a = null;
                for (NExprNodeValue arg : args) {
                    a = arg.value().orNull();
                }
                return a;
            }
        });
        addDefaultOp("(", NExprOpType.POSTFIX, NExprOpPrecedence.PARS, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                throw new IllegalArgumentException("unable to evaluate");
            }
        });
        addDefaultOp("[", NExprOpType.POSTFIX, NExprOpPrecedence.BRACKETS, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                throw new IllegalArgumentException("unable to evaluate");
            }
        });

        addDefaultOp("{", NExprOpType.POSTFIX, NExprOpPrecedence.BRACES, NOperatorAssociativity.LEFT, new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                throw new IllegalArgumentException("unable to evaluate");
            }
        });

        addDefaultFct("string", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                return NLiteral.of(args.get(0).value().orNull()).asString().orNull();
            }
        });
        addDefaultFct("boolean", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                return NLiteral.of(args.get(0).value().orNull()).asBoolean().orNull();
            }
        });
        addDefaultFct("double", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                return NLiteral.of(args.get(0).value().orNull()).asDouble().orNull();
            }
        });
        addDefaultFct("long", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                return NLiteral.of(args.get(0).value().orNull()).asLong().orNull();
            }
        });
        addDefaultFct("int", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                return NLiteral.of(args.get(0).value().orNull()).asInt().orNull();
            }
        });
        addDefaultFct("float", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                return NLiteral.of(args.get(0).value().orNull()).asFloat().orNull();
            }
        });
        addDefaultFct("isNumber", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                return NLiteral.of(args.get(0).value().orNull()).asNumber().isPresent();
            }
        });
        addDefaultFct("isBoolean", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                String name = callContext.name();
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                return NLiteral.of(args.get(0).value().orNull()).asBoolean().isPresent();
            }
        });
        addDefaultFct("isBlank", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                List<NExprNodeValue> args = callContext.args();
                return NBlankable.isBlank(args.get(0).value().orNull());
            }
        });
        addDefaultFct("firstNonNull", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                List<NExprNodeValue> args = callContext.args();
                for (NExprNodeValue arg : args) {
                    Object v = arg.value().orNull();
                    if (v != null) return v;
                }
                return null;
            }
        });
        addDefaultFct("firstNonBlank", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                List<NExprNodeValue> args = callContext.args();
                for (NExprNodeValue arg : args) {
                    Object v = arg.value().orNull();
                    if (!NBlankable.isBlank(v)) return v;
                }
                return null;
            }
        });

        addDefaultFct("format", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                List<NExprNodeValue> args = callContext.args();
                String pattern = String.valueOf(args.get(0).value().orNull());
                Object[] rest = args.subList(1, args.size()).stream()
                        .map(a -> a.value().orNull())
                        .toArray();
                return NMsg.ofC(pattern, rest).toString();
            }
        }, "formatC");

        addDefaultFct("formatJ", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                List<NExprNodeValue> args = callContext.args();
                String pattern = String.valueOf(args.get(0).value().orNull());
                Object[] rest = args.subList(1, args.size()).stream()
                        .map(a -> a.value().orNull())
                        .toArray();
                return NMsg.ofJ(pattern, rest).toString();
            }
        });

        addDefaultFct("formatV", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                List<NExprNodeValue> args = callContext.args();
                String pattern = String.valueOf(args.get(0).value().orNull());
                Object[] rest = args.subList(1, args.size()).stream()
                        .map(a -> a.value().orNull())
                        .toArray();
                if (rest.length == 1) {
                    if (rest[0] instanceof Map) {
                        return NMsg.ofV(pattern, (Map<String, ?>) rest[0]);
                    }
                    if (rest[0] instanceof Function) {
                        return NMsg.ofV(pattern, (Function<String, ?>) rest[0]);
                    }
                    if (rest[0] instanceof NMsgParam[]) {
                        return NMsg.ofV(pattern, (NMsgParam[]) rest[0]);
                    }
                    if (rest[0] instanceof NMsgParam) {
                        return NMsg.ofV(pattern, (NMsgParam) rest[0]);
                    }
                }
                if (rest.length == 0) {
                    return NMsg.ofV(pattern);
                }
                List<NMsgParam> ee = new ArrayList<>();
                for (Object o : rest) {
                    ee.add((NMsgParam) o);
                }
                return NMsg.ofV(pattern, ee.toArray(new NMsgParam[0]));
            }
        });

        addDefaultFct("join", new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                List<NExprNodeValue> args = callContext.args();
                NExprContext context = callContext.context();
                String sep = String.valueOf(args.get(0).value().orNull());
                Object col = args.get(1).value().orNull();
                if (col instanceof Iterable) {
                    List<String> parts = new ArrayList<>();
                    for (Object item : (Iterable<?>) col) {
                        parts.add(String.valueOf(item));
                    }
                    return String.join(sep, parts);
                }
                return String.valueOf(col);
            }
        });
//        addDefaultFct("abs", new NExprCallHandler() {
//            @Override
//            public Object eval(NExprCallContext callContext) {
//                String name = callContext.name();
//                List<NExprNodeValue> args = callContext.args();
//                NExprContext context = callContext.context();
//                NLiteral v = NLiteral.of(args.get(0).value().orNull());
//                if (v.asNumber().isPresent()) {
//                    if (v.isBigDecimal()) {
//                        return v.asBigDecimal().get().abs();
//                    }
//                    if (v.isBigInt()) {
//                        return v.asBigInt().get().abs();
//                    }
//                    if (v.isDouble()) {
//                        return Math.abs(v.asDouble().get());
//                    }
//                    if (v.isFloat()) {
//                        return Math.abs(v.asFloat().get());
//                    }
//                    if (v.isInt()) {
//                        return Math.abs(v.asInt().get());
//                    }
//                    if (v.isShort()) {
//                        short a = v.asShort().get();
//                        return a < 0 ? -a : a;
//                    }
//                    if (v.isByte()) {
//                        byte a = v.asByte().get();
//                        return a < 0 ? -a : a;
//                    }
//                    return v.asBigDecimal().get().abs();
//                }
//                return v.asBoolean().isPresent();
//            }
//        });
        return this;
    }

    public static double asDouble(NOptional<Object> any) {
//        if(any.isError()){
//            rendererContext.log(NMsg.ofC("evaluation error : %s",any.getMessage().get()));
//        }
        return NLiteral.of(any.orNull()).asDouble().orElse(0.0);
    }

    @Override
    public NExprContextBuilder declareMathFunctions() {
        this.declareFunction(NExprFunction.of("sin", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.sin(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("cos", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.cos(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("abs", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.abs(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("tan", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.tan(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("tanh", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.tanh(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("sinh", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.sinh(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("cosh", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.cosh(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("signum", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.signum(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("ulp", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.ulp(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("asin", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.asin(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("acos", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.acos(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("toRadians", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.toRadians(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("toDegrees", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.toDegrees(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("exp", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.exp(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("log", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.log(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("log10", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.log10(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("sqrt", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.sqrt(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("cbrt", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.cbrt(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("ceil", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.ceil(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("floor", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.floor(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("rint", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.rint(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("round", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            return Math.round(asDouble(a.eval(context)));
        }));
        this.declareFunction(NExprFunction.of("atan2", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            NExprNodeValue b = args.get(1);
            return Math.atan2(
                    asDouble(a.eval(context))
                    , asDouble(b.eval(context))
            );
        }));
        this.declareFunction(NExprFunction.of("pow", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            NExprNodeValue b = args.get(1);
            return Math.pow(
                    asDouble(a.eval(context))
                    , asDouble(b.eval(context))
            );
        }));
        this.declareFunction(NExprFunction.of("max", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            NExprNodeValue b = args.get(1);
            return Math.max(
                    asDouble(a.eval(context))
                    , asDouble(b.eval(context))
            );
        }));
        this.declareFunction(NExprFunction.of("min", (callContext) -> {
            List<NExprNodeValue> args = callContext.args();
            NExprContext context = callContext.context();
            NExprNodeValue a = args.get(0);
            NExprNodeValue b = args.get(1);
            return Math.min(
                    asDouble(a.eval(context))
                    , asDouble(b.eval(context))
            );
        }));
        return this;
    }

    public NExprMutableContext buildMutable() {
        return (NExprMutableContext) build(true);
    }

    public NExprContext build() {
        return build(false);
    }

    public NExprContext build(boolean mutable) {
        if (alteration.hasNoCustomDeclarations()) {
            if (mutable) {
                return new NExprMutableContextImpl(rpi, parent);
            }
            return new NExprChildContextImpl(rpi, NExprContextAlteration.EMPTY_RESOLVER, parent);
        }
        if (mutable) {
            return new NExprMutableContextImpl(rpi, alteration, parent);
        }
        return new NExprChildContextImpl(rpi, alteration.toExprResolver(), parent);
    }

    @Override
    public NExprContextBuilder declareFunction(NExprFunction fctImpl) {
        alteration.declareFunction(fctImpl);
        return this;
    }

    @Override
    public NExprContextBuilder declareConstruct(NExprFunction fctImpl) {
        alteration.declareConstruct(fctImpl);
        return this;
    }

    @Override
    public NExprContextBuilder declareOperator(NExprOperator fctImpl) {
        alteration.declareOperator(fctImpl);
        return this;
    }

    @Override
    public NExprContextBuilder declareVars(NExprVarResolver resolver) {
        alteration.declareVars(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder declareConstructs(NExprFunctionResolver resolver) {
        alteration.declareConstructs(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder removeOperators(NExprOperatorResolver resolver) {
        alteration.removeOperators(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder declareFunctions(NExprFunctionResolver resolver) {
        alteration.declareFunctions(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder declareResolver(NExprResolver resolver) {
        alteration.declareResolver(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder removeVars(NExprVarResolver resolver) {
        alteration.removeVars(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder removeConstructs(NExprFunctionResolver resolver) {
        alteration.removeConstructs(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder removeFunctions(NExprFunctionResolver resolver) {
        alteration.removeFunctions(resolver);
        return this;
    }

    @Override
    public NExprContextBuilder removeResolver(NExprResolver resolver) {
        alteration.removeResolver(resolver);
        return this;
    }


    @Override
    public NExprContextBuilder declareOperators(NExprOperatorResolver resolver) {
        alteration.declareOperators(resolver);
        return this;
    }

    public NExprContextBuilder declareConstruct(String name, NExprCallHandler constructImpl) {
        alteration.declareConstruct(name, constructImpl);
        return this;
    }

    public NExprContextBuilder declareFunction(String name, NExprCallHandler fctImpl) {
        alteration.declareFunction(name, fctImpl);
        return this;
    }

    @Override
    public NExprContextBuilder declareVar(NExprVar variable) {
        alteration.declareVar(variable);
        return this;
    }


    @Override
    public NExprContextBuilder declareOperator(String name, NExprCallHandler impl) {
        alteration.declareOperator(name, impl);
        return this;
    }

    @Override
    public NExprContextBuilder declareOperator(String name, NExprOpType type, NExprCallHandler impl) {
        alteration.declareOperator(name, type, impl);
        return this;
    }

    @Override
    public NExprContextBuilder declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprCallHandler impl) {
        alteration.declareOperator(name, type, precedence, associativity, impl);
        return this;
    }

    @Override
    public NExprContextBuilder removeVar(String name) {
        alteration.removeVar(name);
        return this;
    }

    @Override
    public NExprContextBuilder removeFunction(String name) {
        alteration.removeFunction(name);
        return this;
    }

    @Override
    public NExprContextBuilder removeConstruct(String name) {
        alteration.removeConstruct(name);
        return this;
    }

    @Override
    public NExprContextBuilder removeOperator(String name, NExprOpType type) {
        alteration.removeOperator(name, type);
        return this;
    }


    private void addDefaultFct(String id, NExprCallHandler fct, String... names) {
        LinkedHashSet<String> allNames = new LinkedHashSet<>(Arrays.asList(names));
        allNames.add(id);

        for (String name : allNames) {
            declareFunction(NExprFunction.of(name, fct));
        }
    }


    private void addDefaultOp(NExprCommonOp op, NExprOpType opType, int precedence, NOperatorAssociativity acc, String... names) {
        NExprCallHandler h = new NExprCallHandler() {
            @Override
            public Object eval(NExprCallContext callContext) {
                if (callContext.operatorType() == NExprOpType.INFIX) {
                    Object a = callContext.arg(0).get().value().orNull();
                    Object b = callContext.arg(1).get().value().orNull();
                    Class<?> aClass = a == null ? null : a.getClass();
                    Class<?> bClass = b == null ? null : b.getClass();
                    NFunction2 f = callContext.context().findCommonInfixOp(op, aClass, bClass).orNull();
                    if (f != null) {
                        return f.apply(a, b);
                    }
                    throw new IllegalArgumentException("not found infix operator '" + callContext.name() + "'");
                } else if (callContext.operatorType() == NExprOpType.PREFIX) {
                    Object a = callContext.arg(0).get().value().get();
                    NFunction f = callContext.context().findCommonPrefixOp(op
                            , a == null ? null : a.getClass()
                    ).orNull();
                    if (f != null) {
                        return f.apply(a);
                    }
                    throw new IllegalArgumentException("not found prefix operator '" + callContext.name() + "'");
                } else if (callContext.operatorType() == NExprOpType.POSTFIX) {
                    Object a = callContext.arg(0).get().value().get();
                    NFunction f = callContext.context().findCommonPrefixOp(op, a == null ? null : a.getClass()
                    ).orNull();
                    if (f != null) {
                        return f.apply(a);
                    }
                    throw new IllegalArgumentException("not found postfix operator '" + callContext.name() + "'");
                }
                throw new IllegalArgumentException("not found operator '" + callContext.name() + "'");
            }
        };
        Set<String> all=new HashSet<>();
        all.addAll(Arrays.asList(op.image()/*,op.id()*/));
        all.addAll(Arrays.asList(names));
        addDefaultOp(op, opType, precedence, acc, h, all.toArray(new String[0]));
    }

    private void addDefaultOp(NExprCommonOp op, NExprOpType opType, int precedence, NOperatorAssociativity acc, NExprCallHandler h, String... names) {
        LinkedHashSet<String> allNames = new LinkedHashSet<>(Arrays.asList(names));
        /*allNames.add(op.id())*/;
        allNames.add(op.image());
        for (String name : allNames) {
            NExprOperator o = NExprOperator.of(name, opType, precedence, acc, h);
            declareOperator(o);
        }
    }

    private void addDefaultOp(String op, NExprOpType opType, int precedence, NOperatorAssociativity acc, NExprCallHandler h, String... names) {
        LinkedHashSet<String> allNames = new LinkedHashSet<>(Arrays.asList(names));
        allNames.add(op);
        for (String name : allNames) {
            NExprOperator o = NExprOperator.of(name, opType, precedence, acc, h);
            declareOperator(o);
        }
    }


    private Object runDot(Object instance, NExprNodeValue b, NExprContext context) {
        if (instance == null) {
            return null;
        }
        switch (b.nodeType()) {
            case WORD: {
                NExprWordNode w = (NExprWordNode) b.node();
                String n = w.name();
                NReflectType t = NReflectRepository.of().getType(instance.getClass());
                NOptional<NReflectProperty> property = t.getProperty(n);
                if (property.isPresent() && property.get().isRead()) {
                    return property.get().read(instance);
                }
                NOptional<NReflectMethod> method = t.getMethod(n, NReflectSignatureImpl.of());
                if (method.isPresent() && method.get().isAccessible()) {
                    return method.get().invoke(instance);
                }
                if (instance instanceof Map) {
                    return ((Map) instance).get(n);
                }
                throw new NIllegalArgumentException(NMsg.ofC("property not found %s", instance + "." + b));
            }
            case FUNCTION: {
                NExprFunctionNode w = (NExprFunctionNode) b.node();
                String n = w.name();
                NReflectType t = NReflectRepository.of().getType(instance.getClass());
                if (w.getArguments().size() == 0) {
                    NOptional<NReflectMethod> method = t.getMethod(n, NReflectSignatureImpl.of());
                    if (method.isPresent() && method.get().isAccessible()) {
                        return method.get().invoke(instance);
                    }
                    NOptional<NReflectProperty> property = t.getProperty(n);
                    if (property.isPresent() && property.get().isRead()) {
                        return property.get().read(instance);
                    }
                    throw new NIllegalArgumentException(NMsg.ofC("property not found %s", instance + "." + b));
                } else {
                    List<NReflectMethod> methodsByName = t.getMethods().stream().filter(x -> x.getName().equals(n)).collect(Collectors.toList());
                    List<NReflectMethod> found1 = methodsByName.stream().filter(x ->
                            x.getSignature().size() == w.getArguments().size()
                                    || (x.getSignature().isVarArgs() && x.getSignature().size() > w.getArguments().size())
                    ).collect(Collectors.toList());
                    NReflectMethod goodMethod = null;
                    if (found1.size() == 1) {
                        goodMethod = found1.get(0);
                    } else if (found1.size() > 1) {
                        throw new NIllegalArgumentException(NMsg.ofC("too many methods to match %s", w));
                    }
                    if (goodMethod == null) {
                        throw new NIllegalArgumentException(NMsg.ofC("method not found to match  %s", w));
                    }
                    List<Object> values = w.getArguments().stream().map(x -> x.eval(context)).collect(Collectors.toList());
                    NOptional<NReflectMethod> matchingMethod = t.getMatchingMethod(n, NReflectSignatureImpl.of(values.stream().map(x -> x == null ? null : NReflectRepository.of().getType(x.getClass())).toArray(NReflectType[]::new)));
                    goodMethod = matchingMethod.get();
                    return goodMethod.invoke(instance, values.toArray());
                }
            }
            case OPERATOR: {
                NExprOpNode w = (NExprOpNode) b.node();
                String n = w.name();
                if (".".equals(n)) {
                    NExprNode b1 = w.getOperand(0);
                    NExprNode b2 = w.getOperand(1);
                    Object newInstance = runDot(instance, new DefaultNExprNodeValue(b1, context), context);
                    return runDot(newInstance, new DefaultNExprNodeValue(b2, context), context);
                }
                break;
            }
        }
        throw new IllegalArgumentException("unsupported " + instance + "." + b);
    }
}
