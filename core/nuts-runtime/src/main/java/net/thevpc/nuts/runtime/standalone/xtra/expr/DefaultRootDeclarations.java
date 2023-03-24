package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.expr.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRootDeclarations extends NExprDeclarationsBase {
    final Map<String, NExprFctDeclaration> defaultFunctions = new HashMap<>();
    final Map<String, NExprConstructDeclaration> defaultConstructs = new HashMap<>();
    final Map<NExprOpNameAndType, NExprOpDeclaration> ops = new HashMap<>();
    final Map<String, NExprVarDeclaration> defaultVars = new HashMap<>();

    public DefaultRootDeclarations(NSession session) {
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
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NExprDeclarations context) {
                return EvalUtils.castToString(args.get(0));
            }
        }, "string");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NExprDeclarations context) {
                return EvalUtils.castToBoolean(args.get(0));
            }
        }, "boolean");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NExprDeclarations context) {
                return EvalUtils.castToDouble(args.get(0));
            }
        }, "double");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NExprDeclarations context) {
                return EvalUtils.castToLong(args.get(0));
            }
        }, "long");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NExprDeclarations context) {
                return (int) EvalUtils.castToLong(args.get(0));
            }
        }, "int");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NExprDeclarations context) {
                return (float) EvalUtils.castToDouble(args.get(0));
            }
        }, "float");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NExprDeclarations context) {
                return EvalUtils.isNumber(args.get(0));
            }
        }, "isNumber");
        addDefaultFct(new NExprFct() {
            @Override
            public Object eval(String name, List<Object> args, NExprDeclarations context) {
                return EvalUtils.isBoolean(args.get(0));
            }
        }, "isBoolean");
    }

    private void addDefaultFct(NExprFct fct, String... names) {
        for (String name : names) {
            defaultFunctions.put(name, new DefaultNExprFctDeclaration(name, fct));
        }
    }

    private void addDefaultOp(AbstractOp op, String... names) {
        for (String name : names) {
            DefaultNExprOpDeclaration opImpl = new DefaultNExprOpDeclaration(name, op);
            ops.put(new NExprOpNameAndType(name, op.getType()), opImpl);
        }
    }


    private class AndFctNode extends AbstractOp {
        public AndFctNode() {
            super("&&", NExprOpPrecedence.AND, NExprOpAssociativity.LEFT, NExprOpType.INFIX);
        }

        @Override
        public Object eval(String name, List<NExprNode> args, NExprDeclarations context) {
            for (NExprNode arg : args) {
                if (!EvalUtils.castToBoolean(arg.eval(context).get())) {
                    return false;
                }
            }
            return true;
        }
    }

    private class OrFctNode extends AbstractOp {
        public OrFctNode() {
            super("or", NExprOpPrecedence.OR, NExprOpAssociativity.LEFT, NExprOpType.INFIX);
        }

        @Override
        public Object eval(String name, List<NExprNode> args, NExprDeclarations e) {
            for (NExprNode arg : args) {
                if (EvalUtils.castToBoolean(arg.eval(e).get())) {
                    return true;
                }
            }
            return false;
        }
    }

    private class NotFctNode extends AbstractOp {
        public NotFctNode() {
            super("!", NExprOpPrecedence.NOT, NExprOpAssociativity.RIGHT, NExprOpType.PREFIX);
        }

        @Override
        public Object eval(String name, List<NExprNode> args, NExprDeclarations e) {
            return !EvalUtils.castToBoolean(args.get(0).eval(e).get());
        }
    }

    private class LTFctNode extends BinCompareFctNode {

        public LTFctNode() {
            super("lt", NExprOpPrecedence.CMP);
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
            super("lte", NExprOpPrecedence.CMP);
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
            super("gt", NExprOpPrecedence.CMP);
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
            super("gte", NExprOpPrecedence.CMP);
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
            super("eq", NExprOpPrecedence.EQ);
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
            super("neq", NExprOpPrecedence.EQ);
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
            super("plus", NExprOpPrecedence.PLUS);
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
            super("minus", NExprOpPrecedence.PLUS);
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
            super("multiply", NExprOpPrecedence.MUL);
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
            super("divide", NExprOpPrecedence.MUL);
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
    public NOptional<NExprFctDeclaration> getFunction(String fctName, Object... args) {
        return NOptional.of(
                defaultFunctions.get(fctName),
                s -> NMsg.ofC("function not found %s", fctName)
        );
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNode... args) {
        return NOptional.of(
                defaultConstructs.get(constructName),
                s -> NMsg.ofC("construct not found %s", constructName)
        );
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNode... args) {
        return NOptional.of(
                ops.get(new NExprOpNameAndType(opName, type)),
                s -> NMsg.ofC("operator not found %s", opName)
        );
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String varName) {
        return NOptional.of(
                defaultVars.get(varName),
                s -> NMsg.ofC("var not found %s", varName)
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
