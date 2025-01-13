//package net.thevpc.nuts.runtime.standalone.xtra.expr;
//
//import net.thevpc.nuts.expr.*;
//import net.thevpc.nuts.util.NLiteral;
//import net.thevpc.nuts.util.NOptional;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.List;
//
//public abstract class BinArithFctNode extends AbstractOp {
//    public BinArithFctNode(String name, int precedence) {
//        super(name, precedence, NExprOpAssociativity.LEFT, NExprOpType.INFIX);
//    }
//
//    @Override
//    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations e) {
//        NLiteral a = NLiteral.of(args.get(0).eval(e).get());
//        NLiteral b = NLiteral.of(args.get(1).eval(e).get());
//        if (a.asNumber().isPresent() && b.asNumber().isPresent()) {
//            Number na = a.asNumber().get();
//            Number nb = b.asNumber().get();
//
//            if (a.isBigDecimal() || b.isBigDecimal()) {
//                return evalBigDecimal(a.asBigDecimal().get(), b.asBigDecimal().get());
//            }
//            if (a.isBigInt() || b.isBigInt()) {
//                return evalBigInt(a.asBigInt().get(), b.asBigInt().get());
//            }
//            if (a.isDouble() || b.isDouble()) {
//                return evalDouble(a.asDouble().get(), b.asDouble().get());
//            }
//            if (a.isFloat() || b.isFloat()) {
//                if (a.isLong() || b.isLong()) {
//                    return evalDouble(a.asDouble().get(), b.asDouble().get());
//                }
//                return evalDouble(a.asFloat().get(), b.asFloat().get());
//            }
//            if (a.isFloat() || b.isFloat()) {
//                return evalDouble(a.asDouble().get(), b.asDouble().get());
//            }
//            return evalLong(a.asLong().get(), a.asLong().get());
//        }
//        NOptional<Boolean> aBoolean = a.asBoolean();
//        NOptional<Boolean> bBoolean = b.asBoolean();
//        if (aBoolean.isPresent() && bBoolean.isPresent()) {
//            return evalLong(aBoolean.get() ? 1 : 0, bBoolean.get() ? 1 : 0);
//        }
//        String aa = a.asString().orNull();
//        String bb = b.asString().orNull();
//        if (aa == null) {
//            aa = "";
//        }
//        if (bb == null) {
//            bb = "";
//        }
//        throw new IllegalArgumentException("invalid expression: " + a);
//    }
//
//
//    protected abstract long evalLong(long a, long b);
//
//    protected abstract double evalDouble(double a, double b);
//
//    protected abstract BigDecimal evalBigDecimal(BigDecimal a, BigDecimal b);
//
//    protected abstract BigInteger evalBigInt(BigInteger a, BigInteger b);
//}
