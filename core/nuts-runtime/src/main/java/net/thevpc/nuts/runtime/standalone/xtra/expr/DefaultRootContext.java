package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;
import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.runtime.standalone.reflect.NReflectSignatureImpl;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.expr.*;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultRootContext extends NExprContextBase {
    final Map<String, NExprFunction> defaultFunctions = new HashMap<>();
    final Map<String, NExprFunction> defaultConstructs = new HashMap<>();
    final Map<NExprOpNameAndType, NExprOperator> ops = new HashMap<>();
    final Map<String, NExprVar> defaultVars = new HashMap<>();
    private NReflectRepository reflectRepository;

    public DefaultRootContext(NExprRPI nExprRPI) {
        super(nExprRPI);
        reflectRepository = NReflectRepository.of();
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.AND, NExprOpPrecedence.AND, NOperatorAssociativity.LEFT), "&");
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.OR, NExprOpPrecedence.OR, NOperatorAssociativity.LEFT), "|");
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.LT, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.LTE, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.GT, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.GTE, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.EQ, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.NE, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT), "!=", "!==", "<>");
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.PLUS, NExprOpPrecedence.PLUS, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.MINUS, NExprOpPrecedence.PLUS, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.MUL, NExprOpPrecedence.MUL, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.DIV, NExprOpPrecedence.MUL, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.REM, NExprOpPrecedence.CMP, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.XOR, NExprOpPrecedence.OR, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.POW, NExprOpPrecedence.POW, NOperatorAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeBase(NExprCommonOp.DOT, NExprOpPrecedence.DOT, NOperatorAssociativity.LEFT, NExprOpType.INFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                NExprNodeValue a = args.get(0);
                NExprNodeValue b = args.get(1);
                Object instance = a.eval(context).orNull();
                return runDot(instance,b,context);
            }
        });

        addDefaultOp(new NExprCommonOpFctNodePrefix(NExprCommonOp.MINUS, NExprOpPrecedence.NOT, NOperatorAssociativity.RIGHT));
        addDefaultOp(new NExprCommonOpFctNodePrefix(NExprCommonOp.NOT, NExprOpPrecedence.NOT, NOperatorAssociativity.RIGHT));

        addDefaultOp(new NExprCommonOpFctNodeBase(NExprCommonOp.ASSIGN, NExprOpPrecedence.ASSIGN, NOperatorAssociativity.RIGHT, NExprOpType.INFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVar v = context.getVar(varName).get();
                    Object newValue = args.get(1).eval(context).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        for (String relOp : new String[]{"+", "-", "*", "/", "%", "^", "**"}) {
            addDefaultOp(new AbstractOp(relOp + "=", NExprOpPrecedence.ASSIGN, NOperatorAssociativity.RIGHT, NExprOpType.INFIX) {
                @Override
                public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                    NExprNode a = args.get(0);
                    if (a.getType() == NExprNodeType.WORD) {
                        String varName = a.getName();
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

        addDefaultOp(new AbstractOp("++", NExprOpPrecedence.NOT, NOperatorAssociativity.LEFT, NExprOpType.PREFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVar v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("+", context.bindLiteral(oldValue), context.bindLiteral((byte) 1)).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp(new AbstractOp("++", NExprOpPrecedence.NOT, NOperatorAssociativity.LEFT, NExprOpType.POSTFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVar v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("+", context.bindLiteral(oldValue), context.bindLiteral((byte) 1)).get();
                    v.set(newValue, context);
                    return oldValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp(new AbstractOp("--", NExprOpPrecedence.NOT, NOperatorAssociativity.LEFT, NExprOpType.PREFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVar v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("-", context.bindLiteral(oldValue), context.bindLiteral((byte) 1)).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp(new AbstractOp("--", NExprOpPrecedence.NOT, NOperatorAssociativity.LEFT, NExprOpType.POSTFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVar v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("-", context.bindLiteral(oldValue), context.bindLiteral((byte) 1)).get();
                    v.set(newValue, context);
                    return oldValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp(new SemiCommaFctNode(";", NExprOpPrecedence.STATEMENT_SEPARATOR), ";");
        addDefaultOp(new ParsFctNode(), "(");
        addDefaultOp(new BracketsFctNode(), "[");
        addDefaultOp(new BracesFctNode(), "{");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asShort().orNull();
            }
        }, "string");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asBoolean().orNull();
            }
        }, "boolean");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asDouble().orNull();
            }
        }, "double");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asLong().orNull();
            }
        }, "long");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asInt().orNull();
            }
        }, "int");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asFloat().orNull();
            }
        }, "float");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asNumber().isPresent();
            }
        }, "isNumber");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asBoolean().isPresent();
            }
        }, "isBoolean");
        addDefaultFct(new NExprFunctionHandler() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprContext context) {
                NLiteral v = NLiteral.of(args.get(0).getValue().orNull());
                if (v.asNumber().isPresent()) {
                    if (v.isBigDecimal()) {
                        return v.asBigDecimal().get().abs();
                    }
                    if (v.isBigInt()) {
                        return v.asBigInt().get().abs();
                    }
                    if (v.isDouble()) {
                        return Math.abs(v.asDouble().get());
                    }
                    if (v.isFloat()) {
                        return Math.abs(v.asFloat().get());
                    }
                    if (v.isInt()) {
                        return Math.abs(v.asInt().get());
                    }
                    if (v.isShort()) {
                        short a = v.asShort().get();
                        return a < 0 ? -a : a;
                    }
                    if (v.isByte()) {
                        byte a = v.asByte().get();
                        return a < 0 ? -a : a;
                    }
                    return v.asBigDecimal().get().abs();
                }
                return v.asBoolean().isPresent();
            }
        }, "abs");
    }

    private Object runDot(Object instance, NExprNodeValue b, NExprContext context) {
        if (instance == null) {
            return null;
        }
        switch (b.getType()) {
            case WORD: {
                NExprWordNode w = (NExprWordNode) b.getNode();
                String n = w.getName();
                NReflectType t = reflectRepository.getType(instance.getClass());
                NOptional<NReflectProperty> property = t.getProperty(n);
                if (property.isPresent() && property.get().isRead()) {
                    return property.get().read(instance);
                }
                NOptional<NReflectMethod> method = t.getMethod(n, NReflectSignatureImpl.of());
                if (method.isPresent() && method.get().isAccessible()) {
                    return method.get().invoke(instance);
                }
                if(instance instanceof Map ){
                    return ((Map)instance).get(n);
                }
                throw new NIllegalArgumentException(NMsg.ofC("property not found %s", instance + "." + b));
            }
            case FUNCTION: {
                NExprFunctionNode w = (NExprFunctionNode) b.getNode();
                String n = w.getName();
                NReflectType t = reflectRepository.getType(instance.getClass());
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
                    List<Object> values=w.getArguments().stream().map(x->x.eval(context)).collect(Collectors.toList());
                    NOptional<NReflectMethod> matchingMethod = t.getMatchingMethod(n, NReflectSignatureImpl.of(values.stream().map(x -> x == null ? null : reflectRepository.getType(x.getClass())).toArray(NReflectType[]::new)));
                    goodMethod=matchingMethod.get();
                    return goodMethod.invoke(instance,values.toArray());
                }
            }
            case OPERATOR:{
                NExprOpNode w = (NExprOpNode) b.getNode();
                String n = w.getName();
                if(".".equals(n)){
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

    private void addDefaultFct(NExprFunctionHandler fct, String... names) {
        for (String name : names) {
            defaultFunctions.put(name, new DefaultNExprFunction(name, fct));
        }
    }

    private void addDefaultOp(AbstractOp op, String... names) {
        LinkedHashSet<String> allNames = new LinkedHashSet<>(Arrays.asList(names));
        allNames.add(op.getName());
        if (op instanceof NExprCommonOpFctNodeBase) {
            allNames.add(((NExprCommonOpFctNodeBase) op).op.id());
            allNames.add(((NExprCommonOpFctNodeBase) op).op.image());
        }
        for (String name : allNames) {
            DefaultNExprOpDeclaration opImpl = new DefaultNExprOpDeclaration(name, op);
            ops.put(new NExprOpNameAndType(name, op.getType()), opImpl);
        }
    }


    private abstract class NExprCommonOpFctNodeBase extends AbstractOp {
        protected NExprCommonOp op;

        public NExprCommonOpFctNodeBase(NExprCommonOp op, int precedence, NOperatorAssociativity acc, NExprOpType type) {
            super(op.id(), precedence, acc, type);
            this.op = op;
        }

        @Override
        public String toString() {
            return "NExprCommonOpFctNode{" +
                    "name=" + getName() +
                    ",op=" + op +
                    ",type=" + getType() +
                    ",precedence=" + getPrecedence() +
                    ",associativity=" + getAssociativity() +
                    '}';
        }
    }

    private class NExprCommonOpFctNodeInfix extends NExprCommonOpFctNodeBase {
        public NExprCommonOpFctNodeInfix(NExprCommonOp op, int precedence, NOperatorAssociativity acc) {
            super(op, precedence, acc, NExprOpType.INFIX);
        }

        @Override
        public Object eval(String name, List<NExprNodeValue> args, NExprContext e) {
            Object a = args.get(0).eval(e).get();
            Object b = args.get(1).eval(e).get();
            Class<?> aClass = a == null ? null : a.getClass();
            Class<?> bClass = b == null ? null : b.getClass();
            NFunction2 f = e.findCommonInfixOp(op, aClass, bClass).orNull();
            if (f != null) {
                return f.apply(a, b);
            }
            throw new IllegalArgumentException("not found infix operator '" + name + "'");
        }
    }


    private class NExprCommonOpFctNodePrefix extends NExprCommonOpFctNodeBase {
        public NExprCommonOpFctNodePrefix(NExprCommonOp op, int precedence, NOperatorAssociativity acc) {
            super(op, precedence, acc, NExprOpType.PREFIX);
        }

        @Override
        public Object eval(String name, List<NExprNodeValue> args, NExprContext e) {
            Object a = args.get(0).eval(e).get();
            NFunction f = e.findCommonPrefixOp(NExprCommonOp.parse(getName()).get()
                    , a == null ? null : a.getClass()
            ).orNull();
            if (f != null) {
                return f.apply(a);
            }
            throw new IllegalArgumentException("not found prefix operator '" + name + "'");
        }
    }

    private class NExprCommonOpFctNodePostfix extends NExprCommonOpFctNodeBase {
        public NExprCommonOpFctNodePostfix(NExprCommonOp op, int precedence, NOperatorAssociativity acc) {
            super(op, precedence, acc, NExprOpType.POSTFIX);
        }

        @Override
        public Object eval(String name, List<NExprNodeValue> args, NExprContext e) {
            Object a = args.get(0).eval(e).get();
            NFunction f = e.findCommonPrefixOp(NExprCommonOp.parse(getName()).get()
                    , a == null ? null : a.getClass()
            ).orNull();
            if (f != null) {
                return f.apply(a);
            }
            throw new IllegalArgumentException("not found postfix operator '" + name + "'");
        }
    }


    @Override
    public NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue... args) {
        return NOptional.of(
                defaultFunctions.get(fctName),
                () -> NMsg.ofC("function not found %s", fctName)
        );
    }

    @Override
    public NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue... args) {
        return NOptional.of(
                defaultConstructs.get(constructName),
                () -> NMsg.ofC("construct not found %s", constructName)
        );
    }

    @Override
    public NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return NOptional.of(
                ops.get(new NExprOpNameAndType(opName, type)),
                () -> NMsg.ofC("operator not found %s", opName)
        );
    }

    @Override
    public NOptional<NExprVar> getVar(String varName) {
        return NOptional.of(
                defaultVars.get(varName),
                () -> NMsg.ofC("expr var not found %s", varName)
        );
    }

    @Override
    public List<NExprOperator> getOperators() {
        List<NExprOperator> all = new ArrayList<>();
        all.addAll(ops.values());
        return all;
    }

}
