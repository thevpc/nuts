package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRootDeclarations extends NutsExprDeclarationsBase {
    final Map<String, NutsExprFctDeclaration> defaultFunctions = new HashMap<>();
    final Map<String, NutsExprConstructDeclaration> defaultConstructs = new HashMap<>();
    final Map<NutsExprOpNameAndType, NutsExprOpDeclaration> ops = new HashMap<>();
    final Map<String, NutsExprVarDeclaration> defaultVars = new HashMap<>();

    public DefaultRootDeclarations(NutsSession session) {
        setSession(session);
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
        addDefaultOp(new ParsFctNode(), "(");
        addDefaultOp(new BracketsFctNode(), "[");
        addDefaultOp(new BracesFctNode(), "{");
        addDefaultFct(new NutsExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NutsExprDeclarations context) {
                return EvalUtils.castToString(args.get(0));
            }
        }, "string");
        addDefaultFct(new NutsExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NutsExprDeclarations context) {
                return EvalUtils.castToBoolean(args.get(0));
            }
        }, "boolean");
        addDefaultFct(new NutsExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NutsExprDeclarations context) {
                return EvalUtils.castToDouble(args.get(0));
            }
        }, "double");
        addDefaultFct(new NutsExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NutsExprDeclarations context) {
                return EvalUtils.castToLong(args.get(0));
            }
        }, "long");
        addDefaultFct(new NutsExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NutsExprDeclarations context) {
                return (int) EvalUtils.castToLong(args.get(0));
            }
        }, "int");
        addDefaultFct(new NutsExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NutsExprDeclarations context) {
                return (float) EvalUtils.castToDouble(args.get(0));
            }
        }, "float");
        addDefaultFct(new NutsExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NutsExprDeclarations context) {
                return EvalUtils.isNumber(args.get(0));
            }
        }, "isNumber");
        addDefaultFct(new NutsExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NutsExprDeclarations context) {
                return EvalUtils.isBoolean(args.get(0));
            }
        }, "isBoolean");
    }

    private void addDefaultFct(NutsExprFct fct, String... names) {
        for (String name : names) {
            defaultFunctions.put(name, new DefaultNutsExprFctDeclaration(name, fct));
        }
    }

    private void addDefaultOp(AbstractOp op, String... names) {
        for (String name : names) {
            DefaultNutsExprOpDeclaration opImpl = new DefaultNutsExprOpDeclaration(name, op);
            ops.put(new NutsExprOpNameAndType(name, op.getType()), opImpl);
        }
    }


    private class AndFctNode extends AbstractOp {
        public AndFctNode() {
            super("&&", NutsExprOpPrecedence.AND, NutsExprOpAssociativity.LEFT, NutsExprOpType.INFIX);
        }

        @Override
        public Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations context) {
            for (NutsExprNode arg : args) {
                if (!EvalUtils.castToBoolean(arg.eval(context).get())) {
                    return false;
                }
            }
            return true;
        }
    }

    private class OrFctNode extends AbstractOp {
        public OrFctNode() {
            super("or", NutsExprOpPrecedence.OR, NutsExprOpAssociativity.LEFT, NutsExprOpType.INFIX);
        }

        @Override
        public Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations e) {
            for (NutsExprNode arg : args) {
                if (EvalUtils.castToBoolean(arg.eval(e).get())) {
                    return true;
                }
            }
            return false;
        }
    }

    private class NotFctNode extends AbstractOp {
        public NotFctNode() {
            super("!", NutsExprOpPrecedence.NOT, NutsExprOpAssociativity.RIGHT, NutsExprOpType.PREFIX);
        }

        @Override
        public Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations e) {
            return !EvalUtils.castToBoolean(args.get(0).eval(e).get());
        }
    }

    private class LTFctNode extends BinCompareFctNode {

        public LTFctNode() {
            super("lt", NutsExprOpPrecedence.CMP);
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
            super("lte", NutsExprOpPrecedence.CMP);
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
            super("gt", NutsExprOpPrecedence.CMP);
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
            super("gte", NutsExprOpPrecedence.CMP);
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
            super("eq", NutsExprOpPrecedence.EQ);
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
            super("neq", NutsExprOpPrecedence.EQ);
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
            super("plus", NutsExprOpPrecedence.PLUS);
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
            super("minus", NutsExprOpPrecedence.PLUS);
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
            super("multiply", NutsExprOpPrecedence.MUL);
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
            super("divide", NutsExprOpPrecedence.MUL);
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
    public NutsOptional<NutsExprFctDeclaration> getFunction(String fctName, Object... args) {
        return NutsOptional.of(
                defaultFunctions.get(fctName),
                s -> NutsMessage.ofCstyle("function not found %s", fctName)
        );
    }

    @Override
    public NutsOptional<NutsExprConstructDeclaration> getConstruct(String constructName, NutsExprNode... args) {
        return NutsOptional.of(
                defaultConstructs.get(constructName),
                s -> NutsMessage.ofCstyle("construct not found %s", constructName)
        );
    }

    @Override
    public NutsOptional<NutsExprOpDeclaration> getOperator(String opName, NutsExprOpType type, NutsExprNode... args) {
        return NutsOptional.of(
                ops.get(new NutsExprOpNameAndType(opName, type)),
                s -> NutsMessage.ofCstyle("operator not found %s", opName)
        );
    }

    @Override
    public NutsOptional<NutsExprVarDeclaration> getVar(String varName) {
        return NutsOptional.of(
                defaultVars.get(varName),
                s -> NutsMessage.ofCstyle("var not found %s", varName)
        );
    }

    @Override
    public List<NutsExprOpDeclaration> getOperators() {
        List<NutsExprOpDeclaration> all = new ArrayList<>();
        all.addAll(ops.values());
        return all;
    }

    public int[] getOperatorPrecedences() {
        return ops.values().stream().map(NutsExprOpDeclaration::getPrecedence)
                .sorted().distinct().mapToInt(x -> x).toArray();
    }

}
