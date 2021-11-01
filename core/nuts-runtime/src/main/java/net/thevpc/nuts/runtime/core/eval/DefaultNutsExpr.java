package net.thevpc.nuts.runtime.core.eval;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

public class DefaultNutsExpr implements NutsExpr {
    final static Map<String, Fct> defaultFunctions = new HashMap<>();
    final static Map<String, Op> defaultPrefixOps = new HashMap<>();
    final static Map<String, Op> defaultInfixOps = new HashMap<>();
    final static Map<String, Op> defaultPostOps = new HashMap<>();

    private final NutsSession session;
    private final Map<String, NutsExpr.Fct> userFunctions = new LinkedHashMap<>();
    private final Map<String, Boolean> userFunctionsFlag = new HashMap<>();

    private final Map<String, NutsExpr.Op> prefixOps = new LinkedHashMap<>();
    private final Map<String, Boolean> prefixOpsFlag = new HashMap<>();
    private final Map<String, NutsExpr.Op> infixOps = new LinkedHashMap<>();
    private final Map<String, Boolean> infixOpsFlag = new HashMap<>();
    private final Map<String, NutsExpr.Op> postfixOps = new LinkedHashMap<>();
    private final Map<String, Boolean> postfixOpsFlag = new HashMap<>();

    private final Map<String, NutsExpr.Var> userVars = new LinkedHashMap<>();
    private final Map<String, Boolean> userVarsFlag = new HashMap<>();
    private NutsExpr parent;

    {
        addDefaultOp(new AndFctNode(), "and", "&", "&&");
        addDefaultOp(new OrFctNode(), "or", "|", "||");
        addDefaultOp(new NotFctNode(), "not", "!");
        addDefaultOp(new LTFctNode(), "lt", "<");
        addDefaultOp(new LTEFctNode(), "lte", "<=");
        addDefaultOp(new GTFctNode(), "gt", ">");
        addDefaultOp(new GTEFctNode(), "gte", ">=");
        addDefaultOp(new EQFctNode(), "eq", "=", "==");
        addDefaultOp(new NEQFctNode(), "neq", "!=", "!==", "<>");
        addDefaultOp(new PlusFctNode(), "plus", "+");
        addDefaultOp(new MinusFctNode(), "minus", "-");
        addDefaultOp(new MulFctNode(), "multiply", "mul", "*");
        addDefaultOp(new DivFctNode(), "divide", "div", "/");
        addDefaultFct(new Fct() {
            @Override
            public Object eval(String name, Node[] args, NutsExpr context) {
                Object o = args[0].eval(context);
                return EvalUtils.castToString(o);
            }
        }, "string");
        addDefaultFct(new Fct() {
            @Override
            public Object eval(String name, Node[] args, NutsExpr context) {
                Object o = args[0].eval(context);
                return EvalUtils.castToBoolean(o);
            }
        }, "boolean");
        addDefaultFct(new Fct() {
            @Override
            public Object eval(String name, Node[] args, NutsExpr context) {
                Object o = args[0].eval(context);
                return EvalUtils.castToDouble(o);
            }
        }, "double");
        addDefaultFct(new Fct() {
            @Override
            public Object eval(String name, Node[] args, NutsExpr context) {
                Object o = args[0].eval(context);
                return EvalUtils.castToLong(o);
            }
        }, "long");
        addDefaultFct(new Fct() {
            @Override
            public Object eval(String name, Node[] args, NutsExpr context) {
                Object o = args[0].eval(context);
                return (int) EvalUtils.castToLong(o);
            }
        }, "int");
        addDefaultFct(new Fct() {
            @Override
            public Object eval(String name, Node[] args, NutsExpr context) {
                Object o = args[0].eval(context);
                return (float) EvalUtils.castToDouble(o);
            }
        }, "float");
        addDefaultFct(new Fct() {
            @Override
            public Object eval(String name, Node[] args, NutsExpr context) {
                Object o = args[0].eval(context);
                return EvalUtils.isNumber(o);
            }
        }, "isNumber");
        addDefaultFct(new Fct() {
            @Override
            public Object eval(String name, Node[] args, NutsExpr context) {
                Object o = args[0].eval(context);
                return EvalUtils.isBoolean(o);
            }
        }, "isBoolean");
    }

    public DefaultNutsExpr(NutsSession session) {
        this.session = session;
    }

    public DefaultNutsExpr(NutsExpr parent) {
        this.session = parent.getSession();
        this.parent = parent;
    }

    private static void addDefaultFct(Fct fct, String... names) {
        for (String name : names) {
            defaultFunctions.put(name, fct);
        }
    }

    private static void addDefaultOp(AbstractOp op, String... names) {
        for (String name : names) {
            OpImpl opImpl = new OpImpl(name, op.type, op.precedence, op.rightAssociative,op);
            getStaticOps(op.type).put(name, opImpl);
        }
    }

    public static String wrapPars(Node n) {
        if (n instanceof DefaultLiteralNode) {
            String s = n.toString();
            if (s.charAt(0) == '-' || s.charAt(0) == '+') {
                return "(" + s + ")";
            }
            return s;
        }
        if (n instanceof DefaultVarNode) {
            return n.toString();
        }
        if (n instanceof DefaultOpNode) {
            String s = n.toString();
            switch (s.charAt(0)) {
                case '-':
                case '+':
                case '!': {
                    return "(" + s + ")";
                }
            }
            return s;
        }
        return "(" + n + ")";
    }

    public static Map<String, NutsExpr.Op> getStaticOps(NutsExpr.OpType type) {
        return type == NutsExpr.OpType.PREFIX ? DefaultNutsExpr.defaultPrefixOps :
                type == NutsExpr.OpType.INFIX ? DefaultNutsExpr.defaultInfixOps :
                        DefaultNutsExpr.defaultPostOps;
    }

    @Override
    public Node parse(String expression) {
        return new SyntaxParser(expression, getEvalWithCache()).parse();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsExpr.Var getVar(String name) {
        NutsExpr.Var f = userVars.get(name);
        if (f != null) {
            return f;
        }
        Boolean s = userVarsFlag.get(name);
        if (s != null && !s.booleanValue()) {
            return null;
        }
        if (parent != null) {
            return parent.getVar(name);
        } else {
            return null;
        }
    }

    @Override
    public void setFunction(String name, NutsExpr.Fct impl) {
        if (!NutsBlankable.isBlank(name)) {
            if (impl == null) {
                if (userFunctions.containsKey(name)) {
                    userFunctions.remove(name);
                } else {
                    userFunctionsFlag.put(name, false);
                }
            } else {
                userFunctions.remove(name);
            }
        } else {
            userFunctions.put(name, impl);
        }
    }

    @Override
    public void unsetFunction(String name) {
        setFunction(name, null);
    }

    @Override
    public NutsExpr.Fct getFunction(String name) {
        NutsExpr.Fct f = userFunctions.get(name);
        if (f != null) {
            return f;
        }
        Boolean s = userFunctionsFlag.get(name);
        if (s != null && !s.booleanValue()) {
            return null;
        }
        if (parent != null) {
            return parent.getFunction(name);
        } else {
            return DefaultNutsExpr.defaultFunctions.get(name);
        }
    }

    @Override
    public String[] getFunctionNames() {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        for (Map.Entry<String, NutsExpr.Fct> e : userFunctions.entrySet()) {
            all.add(e.getKey());
        }
        if (parent != null) {
            for (String f : parent.getFunctionNames()) {
                Boolean s = userFunctionsFlag.get(f);
                if (s == null || s.booleanValue()) {
                    all.add(f);
                }
            }
        } else {
            for (String f : DefaultNutsExpr.defaultFunctions.keySet()) {
                Boolean s = userFunctionsFlag.get(f);
                if (s == null || s.booleanValue()) {
                    all.add(f);
                }
            }
        }
        return all.toArray(new String[0]);
    }

    @Override
    public Object evalFunction(String fctName, Object... args) {
        NutsExpr.Fct f = getFunction(fctName);
        if (f == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("function not found %s", fctName));
        }
        return f.eval(fctName, Arrays.stream(args).map(DefaultLiteralNode::new).toArray(NutsExpr.Node[]::new), newChild());
    }

    public void setOperator(String name, OpType type, int precedence, boolean rightAssociative, Fct fct) {
        if (NutsBlankable.isBlank(name)) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("empty operator"));
        }
        //TODO: should check supported op!!
        if (fct == null) {
            NutsExpr.OpType[] allTypes = type == null ? NutsExpr.OpType.values() : new NutsExpr.OpType[]{type};
            for (NutsExpr.OpType type0 : allTypes) {
                Map<String, NutsExpr.Op> ops = getOps(type0);
                Map<String, Boolean> opsFlag = getOpsFlag(type0);
                if (ops.containsKey(name)) {
                    ops.remove(name);
                } else {
                    opsFlag.put(name, false);
                }
            }
        } else {
            if (type == null) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing op type"));
            }
            Map<String, NutsExpr.Op> ops = getOps(type);
            ops.put(name, new OpImpl(name, type, precedence, rightAssociative, fct));
        }
    }

    @Override
    public NutsExpr.Op getOperator(String opName, NutsExpr.OpType type) {
        NutsExpr.Op f = getOps(type).get(opName);
        if (f != null) {
            return f;
        }
        Boolean b = getOpsFlag(type).get(opName);
        if (b != null && !b) {
            return null;
        }
        if (parent != null) {
            return parent.getOperator(opName, type);
        } else {
            return getStaticOps(type).get(opName);
        }
    }

    @Override
    public void unsetOperator(String name, OpType type) {
        setOperator(name, type, -1, false, null);
    }

    @Override
    public String[] getOperatorNames(NutsExpr.OpType type) {
        if (type == null) {
            LinkedHashSet<String> all = new LinkedHashSet<>();
            for (OpType value : OpType.values()) {
                all.addAll(Arrays.asList(getOperatorNames(value)));
            }
            return all.toArray(new String[0]);
        }
        LinkedHashSet<String> all = new LinkedHashSet<>();
        Map<String, NutsExpr.Op> ops = getOps(type);

        Map<String, Boolean> opsFlag = getOpsFlag(type);

        for (Map.Entry<String, NutsExpr.Op> e : ops.entrySet()) {
            all.add(e.getKey());
        }
        if (parent != null) {
            for (String f : parent.getFunctionNames()) {
                Boolean s = opsFlag.get(f);
                if (s == null || s.booleanValue()) {
                    all.add(f);
                }
            }
        } else {
            for (String f :
                    getStaticOps(type)
                            .keySet()) {
                Boolean s = opsFlag.get(f);
                if (s == null || s.booleanValue()) {
                    all.add(f);
                }
            }
        }
        return all.toArray(new String[0]);
    }

    @Override
    public void setVar(String name, NutsExpr.Var impl) {
        if (!NutsBlankable.isBlank(name)) {
            if (impl == null) {
                if (userVars.containsKey(name)) {
                    userVars.remove(name);
                } else {
                    userVarsFlag.put(name, false);
                }
            } else {
                userVars.remove(name);
            }
        } else {
            userVars.put(name, impl);
        }
    }

    @Override
    public Object evalVar(String fctName) {
        NutsExpr.Var f = getVar(fctName);
        if (f == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("var not found %s", fctName));
        }
        return f.get(fctName, this);
    }

    public NutsExpr newChild() {
        return new DefaultNutsExpr(this);
    }

    @Override
    public Object evalNode(NutsExpr.Node node) {
        return node.eval(this);
    }

    @NotNull
    private NutsExprWithCache getEvalWithCache() {
        return new NutsExprWithCache(this);
    }

    private Map<String, Boolean> getOpsFlag(NutsExpr.OpType type) {
        return type == NutsExpr.OpType.PREFIX ? this.prefixOpsFlag :
                type == NutsExpr.OpType.INFIX ? this.infixOpsFlag :
                        postfixOpsFlag;
    }

    private Map<String, NutsExpr.Op> getOps(NutsExpr.OpType type) {
        return type == NutsExpr.OpType.PREFIX ? this.prefixOps :
                type == NutsExpr.OpType.INFIX ? this.infixOps :
                        postfixOps;
    }

    private abstract class BinCompareFctNode extends AbstractOp {
        public BinCompareFctNode(String name, int precedence) {
            super(name, precedence, false,OpType.INFIX);
        }

        @Override
        public Object eval(String name, Node[] args, NutsExpr e) {
            Object a = args[0].eval(e);
            Object b = args[1].eval(e);
            if (EvalUtils.isNumber(a) && EvalUtils.isNumber(b)) {
                return compare(EvalUtils.castToNumber(a).doubleValue(), EvalUtils.castToNumber(b).doubleValue());
            }
            if (EvalUtils.isBoolean(a) && EvalUtils.isBoolean(b)) {
                return compare(EvalUtils.castToNumber(a).doubleValue(), EvalUtils.castToNumber(b).doubleValue());
            }
            String aa = EvalUtils.castToString(a);
            String bb = EvalUtils.castToString(b);
            if (aa == null) {
                aa = "";
            }
            if (bb == null) {
                bb = "";
            }
            return compare(aa.length(), bb.length());
        }

        protected final boolean compare(Number a, Number b) {
            if (EvalUtils.isBig(a) || EvalUtils.isBig(b)) {
                if (EvalUtils.isFloat(a) || EvalUtils.isFloat(b)) {
                    BigDecimal aa = (a instanceof BigDecimal) ? ((BigDecimal) a) : new BigDecimal(a.toString());
                    BigDecimal bb = (b instanceof BigDecimal) ? ((BigDecimal) b) : new BigDecimal(b.toString());
                    return evalBigDecimal(aa, bb);
                }
                BigInteger aa = (a instanceof BigInteger) ? ((BigInteger) a) : new BigInteger(a.toString());
                BigInteger bb = (b instanceof BigInteger) ? ((BigInteger) b) : new BigInteger(b.toString());
                return evalBigInteger(aa, bb);
            } else {
                if (EvalUtils.isFloat(a) || EvalUtils.isFloat(b)) {
                    double aa = a.doubleValue();
                    double bb = b.doubleValue();
                    return evalFloat(aa, bb);
                }
                long aa = a.longValue();
                long bb = b.longValue();
                return evalOrdinal(aa, bb);
            }
        }

        protected abstract boolean evalOrdinal(long a, long b);

        protected abstract boolean evalFloat(double a, double b);

        protected abstract boolean evalBigDecimal(BigDecimal a, BigDecimal b);

        protected abstract boolean evalBigInteger(BigInteger a, BigInteger b);
    }

    private abstract class BinArithFctNode extends AbstractOp {
        public BinArithFctNode(String name, int precedence) {
            super(name, precedence, false,OpType.INFIX);
        }

        @Override
        public Object eval(String name, Node[] args, NutsExpr e) {
            Object a = args[0].eval(e);
            Object b = args[1].eval(e);
            if (EvalUtils.isNumber(a) && EvalUtils.isNumber(b)) {
                return evalAny(EvalUtils.castToNumber(a).doubleValue(), EvalUtils.castToNumber(b).doubleValue());
            }
            if (EvalUtils.isBoolean(a) && EvalUtils.isBoolean(b)) {
                return evalAny(EvalUtils.castToNumber(a).doubleValue(), EvalUtils.castToNumber(b).doubleValue());
            }
            String aa = EvalUtils.castToString(a);
            String bb = EvalUtils.castToString(b);
            if (aa == null) {
                aa = "";
            }
            if (bb == null) {
                bb = "";
            }
            return evalAny(aa.length(), bb.length());
        }

        protected final Number evalAny(Number a, Number b) {
            if (EvalUtils.isBig(a) || EvalUtils.isBig(b)) {
                if (EvalUtils.isFloat(a) || EvalUtils.isFloat(b)) {
                    BigDecimal aa = (a instanceof BigDecimal) ? ((BigDecimal) a) : new BigDecimal(a.toString());
                    BigDecimal bb = (b instanceof BigDecimal) ? ((BigDecimal) b) : new BigDecimal(b.toString());
                    return evalBigDecimal(aa, bb);
                }
                BigInteger aa = (a instanceof BigInteger) ? ((BigInteger) a) : new BigInteger(a.toString());
                BigInteger bb = (b instanceof BigInteger) ? ((BigInteger) b) : new BigInteger(b.toString());
                return evalBigInteger(aa, bb);
            } else {
                if (EvalUtils.isFloat(a) || EvalUtils.isFloat(b)) {
                    double aa = a.doubleValue();
                    double bb = b.doubleValue();
                    return evalFloat(aa, bb);
                }
                long aa = a.longValue();
                long bb = b.longValue();
                return evalOrdinal(aa, bb);
            }
        }

        protected abstract long evalOrdinal(long a, long b);

        protected abstract double evalFloat(double a, double b);

        protected abstract BigDecimal evalBigDecimal(BigDecimal a, BigDecimal b);

        protected abstract BigInteger evalBigInteger(BigInteger a, BigInteger b);
    }

    private abstract class AbstractOp implements Fct {
        private final OpType type;
        private final String name;
        private final int precedence;
        private final boolean rightAssociative;

        public AbstractOp(String name, int precedence, boolean rightAssociative,OpType type) {
            this.name = name;
            this.type = type;
            this.precedence = precedence;
            this.rightAssociative = rightAssociative;
        }

        public OpType getOpType() {
            return type;
        }

        public int getPrecedence() {
            return precedence;
        }
    }

    private class AndFctNode extends AbstractOp {
        public AndFctNode() {
            super("&&", 40, false,OpType.INFIX);
        }

        @Override
        public Object eval(String name, Node[] args, NutsExpr context) {
            for (Node arg : args) {
                if (!EvalUtils.castToBoolean(arg.eval(context))) {
                    return false;
                }
            }
            return true;
        }
    }

    private class OrFctNode extends AbstractOp {
        public OrFctNode() {
            super("or", 30,false, OpType.INFIX);
        }

        @Override
        public Object eval(String name, Node[] args, NutsExpr e) {
            for (Node arg : args) {
                if (EvalUtils.castToBoolean(arg.eval(e))) {
                    return true;
                }
            }
            return false;
        }
    }

    private class NotFctNode extends AbstractOp {
        public NotFctNode() {
            super("!", 130,true, OpType.PREFIX);
        }

        @Override
        public Object eval(String name, Node[] args, NutsExpr e) {
            return !EvalUtils.castToBoolean(args[0].eval(e));
        }
    }

    private class LTFctNode extends BinCompareFctNode {

        public LTFctNode() {
            super("lt", 90);
        }

        @Override
        protected boolean evalOrdinal(long a, long b) {
            return a < b;
        }

        @Override
        protected boolean evalFloat(double a, double b) {
            return a < b;
        }

        @Override
        protected boolean evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.compareTo(b) < 0;
        }

        @Override
        protected boolean evalBigInteger(BigInteger a, BigInteger b) {
            return a.compareTo(b) < 0;
        }
    }

    private class LTEFctNode extends BinCompareFctNode {
        public LTEFctNode() {
            super("lte", 90);
        }

        @Override
        protected boolean evalOrdinal(long a, long b) {
            return a <= b;
        }

        @Override
        protected boolean evalFloat(double a, double b) {
            return a <= b;
        }

        @Override
        protected boolean evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.compareTo(b) <= 0;
        }

        @Override
        protected boolean evalBigInteger(BigInteger a, BigInteger b) {
            return a.compareTo(b) <= 0;
        }
    }

    private class GTFctNode extends BinCompareFctNode {
        public GTFctNode() {
            super("gt", 90);
        }

        @Override
        protected boolean evalOrdinal(long a, long b) {
            return a > b;
        }

        @Override
        protected boolean evalFloat(double a, double b) {
            return a > b;
        }

        @Override
        protected boolean evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.compareTo(b) > 0;
        }

        @Override
        protected boolean evalBigInteger(BigInteger a, BigInteger b) {
            return a.compareTo(b) > 0;
        }
    }

    private class GTEFctNode extends BinCompareFctNode {
        public GTEFctNode() {
            super("gte", 90);
        }

        @Override
        protected boolean evalOrdinal(long a, long b) {
            return a >= b;
        }

        @Override
        protected boolean evalFloat(double a, double b) {
            return a >= b;
        }

        @Override
        protected boolean evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.compareTo(b) >= 0;
        }

        @Override
        protected boolean evalBigInteger(BigInteger a, BigInteger b) {
            return a.compareTo(b) >= 0;
        }
    }

    private class EQFctNode extends BinCompareFctNode {
        public EQFctNode() {
            super("eq", 80);
        }

        @Override
        protected boolean evalOrdinal(long a, long b) {
            return a == b;
        }

        @Override
        protected boolean evalFloat(double a, double b) {
            return a == b;
        }

        @Override
        protected boolean evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.compareTo(b) == 0;
        }

        @Override
        protected boolean evalBigInteger(BigInteger a, BigInteger b) {
            return a.compareTo(b) == 0;
        }
    }

    private class NEQFctNode extends BinCompareFctNode {
        public NEQFctNode() {
            super("neq", 80);
        }

        @Override
        protected boolean evalOrdinal(long a, long b) {
            return a != b;
        }

        @Override
        protected boolean evalFloat(double a, double b) {
            return a != b;
        }

        @Override
        protected boolean evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.compareTo(b) != 0;
        }

        @Override
        protected boolean evalBigInteger(BigInteger a, BigInteger b) {
            return a.compareTo(b) != 0;
        }
    }

    private class PlusFctNode extends BinArithFctNode {
        public PlusFctNode() {
            super("plus", 110);
        }

        @Override
        protected long evalOrdinal(long a, long b) {
            return a + b;
        }

        @Override
        protected double evalFloat(double a, double b) {
            return a + b;
        }

        @Override
        protected BigDecimal evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.add(b);
        }

        @Override
        protected BigInteger evalBigInteger(BigInteger a, BigInteger b) {
            return a.add(b);
        }
    }

    private class MinusFctNode extends BinArithFctNode {
        public MinusFctNode() {
            super("minus", 110);
        }

        @Override
        protected long evalOrdinal(long a, long b) {
            return a - b;
        }

        @Override
        protected double evalFloat(double a, double b) {
            return a - b;
        }

        @Override
        protected BigDecimal evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.subtract(b);
        }

        @Override
        protected BigInteger evalBigInteger(BigInteger a, BigInteger b) {
            return a.subtract(b);
        }
    }

    private class MulFctNode extends BinArithFctNode {
        public MulFctNode() {
            super("multiply", 120);
        }

        @Override
        protected long evalOrdinal(long a, long b) {
            return a * b;
        }

        @Override
        protected double evalFloat(double a, double b) {
            return a * b;
        }

        @Override
        protected BigDecimal evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.multiply(b);
        }

        @Override
        protected BigInteger evalBigInteger(BigInteger a, BigInteger b) {
            return a.multiply(b);
        }
    }

    private class DivFctNode extends BinArithFctNode {
        public DivFctNode() {
            super("divide", 120);
        }

        @Override
        protected long evalOrdinal(long a, long b) {
            return a / b;
        }

        @Override
        protected double evalFloat(double a, double b) {
            return a / b;
        }

        @Override
        protected BigDecimal evalBigDecimal(BigDecimal a, BigDecimal b) {
            return a.divide(b, RoundingMode.HALF_EVEN);
        }

        @Override
        protected BigInteger evalBigInteger(BigInteger a, BigInteger b) {
            return a.divide(b);
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }
}

