package net.thevpc.nuts.toolbox.nwork.filescanner.eval;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SimpleEvaluator implements Evaluator {
    private static String wrapPars(Node n) {
        if (n instanceof LiteralNode) {
            String s = n.toString();
            if(s.charAt(0)=='-' || s.charAt(0)=='+'){
                return "(" + s + ")";
            }
            return s;
        }
        if (n instanceof VarNode) {
            return n.toString();
        }
        if (n instanceof FctNode) {
            String s = n.toString();
            switch (s.charAt(0)){
                case '-':
                case '+':
                case '!':{
                    return "(" + s + ")";
                }
            }
            return s;
        }
        return "(" + n + ")";
    }

    @Override
    public Node createVar(String name) {
        return new VarNode(name);
    }

    @Override
    public Node createLiteral(Object lit) {
        return new LiteralNode(lit);
    }

    public Node createFunction(String name, Node[] args) {
        switch (name) {
            case "and": {
                return new AddFctNode(args);
            }
            case "or": {
                return new OrFctNode(args);
            }
            case "not": {
                return new NotFctNode(args);
            }
            case "<": {
                return new LTFctNode(name, args);
            }
            case "<=": {
                return new LTEFctNode(name, args);
            }
            case ">": {
                return new GTFctNode(name, args);
            }
            case ">=": {
                return new GTEFctNode(name, args);
            }
            case "=":
            case "==": {
                return new EQFctNode(name, args);
            }
            case "<>":
            case "!=": {
                return new NEQFctNode(name, args);
            }
            case "+": {
                return new PlusFctNode(name, args);
            }
            case "-": {
                return new MinusFctNode(name, args);
            }
            case "*": {
                return new MulFctNode(name, args);
            }
            case "/": {
                return new DivFctNode(name, args);
            }
            case "tag": {
            }
        }
        return new CustomFctNode(name, args);
    }

    public Object evalFunction(FctNode functionNode, Context context) {
        throw new IllegalArgumentException("unsupported function " + functionNode.getName());
    }

    public enum FctNodeType {
        FCT, PRFIX_OP, INFIX_OP
    }

    public abstract static class FctNode implements Node {
        private final String name;
        private final Node[] args;
        private final FctNodeType op;

        public FctNode(FctNodeType op, String name, Node[] args) {
            this.op = op;
            this.name = name;
            this.args = args;
        }

        public String getName() {
            return name;
        }

        public Node getArg(int index) {
            if (index >= args.length) {
                throw new IllegalArgumentException("Missing argument " + (index + 1) + " for " + name);
            }
            return args[index];
        }

        public Node[] getArgs() {
            return args;
        }

        public abstract Object eval(Context context);

        @Override
        public String toString() {
            switch (op) {
                case PRFIX_OP: {
                    return name+" "+wrapPars(args[0]);
                }
                case INFIX_OP: {
                    if (args.length == 2) {
                        return wrapPars(args[0]) + " " + name + " " + wrapPars(args[1]);
                    }
                    if (args.length >2) {
                        StringBuilder sb=new StringBuilder();
                        for (int i = 0; i < args.length; i++) {
                            if(i>0){
                                sb.append(" " + name + " ");
                            }
                            sb.append(wrapPars(args[i]));
                        }
                        return  sb.toString();
                    }
                }
            }
            return name + "(" +
                    Arrays.stream(args).map(Object::toString).collect(Collectors.joining(",")) +
                    ')';
        }
    }

    private static class LiteralNode implements Node {
        private final Object lit;

        public LiteralNode(Object lit) {
            this.lit = lit;
        }

        @Override
        public Object eval(Context context) {
            return lit;
        }

        @Override
        public String toString() {
            if(lit==null){
                return "null";
            }
            if(lit instanceof String){
                StringBuilder sb=new StringBuilder("\"");
                for (char c : lit.toString().toCharArray()) {
                    switch (c){
                        case '"':{
                            sb.append("\\\"");
                            break;
                        }
                        case '\\':{
                            sb.append("\\\\");
                            break;
                        }
                        case '\n':{
                            sb.append("\\n");
                            break;
                        }
                        case '\r':{
                            sb.append("\\r");
                            break;
                        }
                        default:{
                            sb.append(c);
                        }
                    }
                }
                sb.append("\"");
                return sb.toString();
            }
            return String.valueOf(lit);
        }
    }

    private static class VarNode implements Node {
        private final String name;

        public VarNode(String name) {
            this.name = name;
        }

        @Override
        public Object eval(Context context) {
            return context.getVar(name);
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }
    }

    private abstract class BinCompareFctNode extends FctNode {
        public BinCompareFctNode(String name, Node[] args) {
            super(FctNodeType.INFIX_OP, name, args);
        }

        @Override
        public Object eval(Context e) {
            Object a = getArg(0).eval(e);
            Object b = getArg(1).eval(e);
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

    private abstract class BinArithFctNode extends FctNode {
        public BinArithFctNode(String name, Node[] args) {
            super(FctNodeType.INFIX_OP, name, args);
        }

        @Override
        public Object eval(Context e) {
            Object a = getArg(0).eval(e);
            Object b = getArg(1).eval(e);
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

    private class AddFctNode extends FctNode {
        public AddFctNode(Node[] args) {
            super(FctNodeType.INFIX_OP, "and", args);
        }

        @Override
        public Object eval(Context e) {
            for (Node arg : getArgs()) {
                if (!EvalUtils.castToBoolean(arg.eval(e))) {
                    return false;
                }
            }
            return true;
        }
    }

    private class OrFctNode extends FctNode {
        public OrFctNode(Node[] args) {
            super(FctNodeType.INFIX_OP, "or", args);
        }

        @Override
        public Object eval(Context e) {
            for (Node arg : getArgs()) {
                if (EvalUtils.castToBoolean(arg.eval(e))) {
                    return true;
                }
            }
            return false;
        }
    }

    private class NotFctNode extends FctNode {
        public NotFctNode(Node[] args) {
            super(FctNodeType.PRFIX_OP, "not", args);
        }

        @Override
        public Object eval(Context e) {
            return !EvalUtils.castToBoolean(getArg(0).eval(e));
        }
    }

    private class LTFctNode extends BinCompareFctNode {

        public LTFctNode(String name, Node[] args) {
            super(name, args);
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
        public LTEFctNode(String name, Node[] args) {
            super(name, args);
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
        public GTFctNode(String name, Node[] args) {
            super(name, args);
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
        public GTEFctNode(String name, Node[] args) {
            super(name, args);
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
        public EQFctNode(String name, Node[] args) {
            super(name, args);
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
        public NEQFctNode(String name, Node[] args) {
            super(name, args);
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
        public PlusFctNode(String name, Node[] args) {
            super(name, args);
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
        public MinusFctNode(String name, Node[] args) {
            super(name, args);
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
        public MulFctNode(String name, Node[] args) {
            super(name, args);
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
        public DivFctNode(String name, Node[] args) {
            super(name, args);
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

    private class CustomFctNode extends FctNode {
        public CustomFctNode(String name, Node[] args) {
            super(FctNodeType.FCT, name, args);
        }

        @Override
        public Object eval(Context context) {
            return evalFunction(this, context);
        }
    }
}
