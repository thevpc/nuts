package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.expr.*;

import java.util.*;

public class DefaultRootDeclarations extends NExprDeclarationsBase {
    final Map<String, NExprFctDeclaration> defaultFunctions = new HashMap<>();
    final Map<String, NExprConstructDeclaration> defaultConstructs = new HashMap<>();
    final Map<NExprOpNameAndType, NExprOpDeclaration> ops = new HashMap<>();
    final Map<String, NExprVarDeclaration> defaultVars = new HashMap<>();
    private NReflectRepository reflectRepository;
    public DefaultRootDeclarations(NExprs exprs, NWorkspace workspace) {
        super(exprs, workspace);
        reflectRepository=NReflectRepository.of();
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.AND, NExprOpPrecedence.AND, NExprOpAssociativity.LEFT), "&");
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.OR, NExprOpPrecedence.OR, NExprOpAssociativity.LEFT), "|");
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.LT, NExprOpPrecedence.CMP, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.LTE, NExprOpPrecedence.CMP, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.GT, NExprOpPrecedence.CMP, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.GTE, NExprOpPrecedence.CMP, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.EQ, NExprOpPrecedence.CMP, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.NE, NExprOpPrecedence.CMP, NExprOpAssociativity.LEFT), "!=", "!==", "<>");
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.PLUS, NExprOpPrecedence.PLUS, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.MINUS, NExprOpPrecedence.PLUS, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.MUL, NExprOpPrecedence.MUL, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.DIV, NExprOpPrecedence.MUL, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.REM, NExprOpPrecedence.CMP, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.XOR, NExprOpPrecedence.OR, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeInfix(NExprCommonOp.POW, NExprOpPrecedence.POW, NExprOpAssociativity.LEFT));
        addDefaultOp(new NExprCommonOpFctNodeBase(NExprCommonOp.DOT, NExprOpPrecedence.DOT, NExprOpAssociativity.LEFT, NExprOpType.INFIX){
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NExprNodeValue a = args.get(0);
                NExprNodeValue b = args.get(1);
                Object instance = a.eval(context).orNull();
                if(instance==null){
                    return null;
                }
                switch (b.getType()){
                    case WORD:{
                        NExprWordNode w=(NExprWordNode)b.getNode();
                        String n = w.getName();
                        NReflectType t = reflectRepository.getType(instance.getClass());
                        NOptional<NReflectProperty> property = t.getProperty(n);
                        if(property.isPresent()) {
                            return property.get().read(instance);
                        }
                        NOptional<NReflectMethod> method = t.getMethod(n, NSignature.of());
                        if(method.isPresent()){
                            return method.get().invoke(instance);
                        }
                        throw new IllegalArgumentException("property not found "+instance+"."+b);
                    }
                }
                throw new IllegalArgumentException("unsupported "+instance+"."+b);
            }
        });

        addDefaultOp(new NExprCommonOpFctNodePrefix(NExprCommonOp.MINUS, NExprOpPrecedence.NOT, NExprOpAssociativity.RIGHT));
        addDefaultOp(new NExprCommonOpFctNodePrefix(NExprCommonOp.NOT, NExprOpPrecedence.NOT, NExprOpAssociativity.RIGHT));

        addDefaultOp(new NExprCommonOpFctNodeBase(NExprCommonOp.ASSIGN, NExprOpPrecedence.ASSIGN, NExprOpAssociativity.RIGHT, NExprOpType.INFIX){
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVarDeclaration v = context.getVar(varName).get();
                    Object newValue = args.get(1).eval(context).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        for (String relOp : new String[]{"+", "-", "*", "/", "%", "^", "**"}) {
            addDefaultOp(new AbstractOp(relOp + "=", NExprOpPrecedence.ASSIGN, NExprOpAssociativity.RIGHT, NExprOpType.INFIX) {
                @Override
                public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                    NExprNode a = args.get(0);
                    if (a.getType() == NExprNodeType.WORD) {
                        String varName = a.getName();
                        NExprVarDeclaration v = context.getVar(varName).get();
                        Object oldValue = v.get(context);
                        Object partValue = args.get(1).eval(context).get();
                        Object newValue = context.evalInfixOperator(relOp, context.literalAsValue(oldValue), context.literalAsValue(partValue)).get();
                        v.set(newValue, context);
                        return newValue;
                    }
                    throw new IllegalArgumentException("cannot assign to non valid variable " + name);
                }
            });
        }

        addDefaultOp(new AbstractOp("++", NExprOpPrecedence.NOT, NExprOpAssociativity.LEFT, NExprOpType.PREFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVarDeclaration v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("+", context.literalAsValue(oldValue), context.literalAsValue((byte) 1)).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp(new AbstractOp("++", NExprOpPrecedence.NOT, NExprOpAssociativity.LEFT, NExprOpType.POSTFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVarDeclaration v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("+", context.literalAsValue(oldValue), context.literalAsValue((byte) 1)).get();
                    v.set(newValue, context);
                    return oldValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp(new AbstractOp("--", NExprOpPrecedence.NOT, NExprOpAssociativity.LEFT, NExprOpType.PREFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVarDeclaration v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("-", context.literalAsValue(oldValue), context.literalAsValue((byte) 1)).get();
                    v.set(newValue, context);
                    return newValue;
                }
                throw new IllegalArgumentException("cannot assign to non valid variable " + name);
            }
        });

        addDefaultOp(new AbstractOp("--", NExprOpPrecedence.NOT, NExprOpAssociativity.LEFT, NExprOpType.POSTFIX) {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NExprNode a = args.get(0);
                if (a.getType() == NExprNodeType.WORD) {
                    String varName = a.getName();
                    NExprVarDeclaration v = context.getVar(varName).get();
                    Object oldValue = v.get(context);
                    Object newValue = context.evalInfixOperator("-", context.literalAsValue(oldValue), context.literalAsValue((byte) 1)).get();
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
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asShort().orNull();
            }
        }, "string");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asBoolean().orNull();
            }
        }, "boolean");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asDouble().orNull();
            }
        }, "double");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asLong().orNull();
            }
        }, "long");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asInt().orNull();
            }
        }, "int");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asFloat().orNull();
            }
        }, "float");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asNumber().isPresent();
            }
        }, "isNumber");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                return NLiteral.of(args.get(0).getValue().orNull()).asBoolean().isPresent();
            }
        }, "isBoolean");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NLiteral v = NLiteral.of(args.get(0).getValue().orNull());
                if (v.isNumber()) {
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

    private void addDefaultFct(NExprFct fct, String... names) {
        for (String name : names) {
            defaultFunctions.put(name, new DefaultNExprFctDeclaration(name, fct));
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

        public NExprCommonOpFctNodeBase(NExprCommonOp op, int precedence, NExprOpAssociativity acc, NExprOpType type) {
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
        public NExprCommonOpFctNodeInfix(NExprCommonOp op, int precedence, NExprOpAssociativity acc) {
            super(op, precedence, acc, NExprOpType.INFIX);
        }

        @Override
        public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations e) {
            Object a = args.get(0).eval(e).get();
            Object b = args.get(1).eval(e).get();
            Class<?> aClass = a == null ? null : a.getClass();
            Class<?> bClass = b == null ? null : b.getClass();
            NFunction2 f = e.findCommonInfixOp(op, aClass, bClass).orNull();
            if (f != null) {
                return f.apply(a, b);
            }
            throw new IllegalArgumentException("not found a "+name+" b");
        }
    }


    private class NExprCommonOpFctNodePrefix extends NExprCommonOpFctNodeBase {
        public NExprCommonOpFctNodePrefix(NExprCommonOp op, int precedence, NExprOpAssociativity acc) {
            super(op, precedence, acc, NExprOpType.PREFIX);
        }

        @Override
        public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations e) {
            Object a = args.get(0).eval(e).get();
            NFunction f = e.findCommonPrefixOp(NExprCommonOp.parse(getName()).get()
                    , a == null ? null : a.getClass()
            ).orNull();
            if (f != null) {
                return f.apply(a);
            }
            throw new IllegalArgumentException("not found a "+name);
        }
    }

    private class NExprCommonOpFctNodePostfix extends NExprCommonOpFctNodeBase {
        public NExprCommonOpFctNodePostfix(NExprCommonOp op, int precedence, NExprOpAssociativity acc) {
            super(op, precedence, acc, NExprOpType.POSTFIX);
        }

        @Override
        public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations e) {
            Object a = args.get(0).eval(e).get();
            NFunction f = e.findCommonPrefixOp(NExprCommonOp.parse(getName()).get()
                    , a == null ? null : a.getClass()
            ).orNull();
            if (f != null) {
                return f.apply(a);
            }
            throw new IllegalArgumentException("not found "+name+" a");
        }
    }


    @Override
    public NOptional<NExprFctDeclaration> getFunction(String fctName, NExprNodeValue... args) {
        return NOptional.of(
                defaultFunctions.get(fctName),
                () -> NMsg.ofC("function not found %s", fctName)
        );
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNodeValue... args) {
        return NOptional.of(
                defaultConstructs.get(constructName),
                () -> NMsg.ofC("construct not found %s", constructName)
        );
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return NOptional.of(
                ops.get(new NExprOpNameAndType(opName, type)),
                () -> NMsg.ofC("operator not found %s", opName)
        );
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String varName) {
        return NOptional.of(
                defaultVars.get(varName),
                () -> NMsg.ofC("var not found %s", varName)
        );
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        List<NExprOpDeclaration> all = new ArrayList<>();
        all.addAll(ops.values());
        return all;
    }

    public int[] getOperatorPrecedences() {
        return ops.values().stream().map(NExprOpDeclaration::getPrecedence)
                .sorted().distinct().mapToInt(x -> x).toArray();
    }

}
