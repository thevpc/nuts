//package net.thevpc.nuts.runtime.standalone.xtra.expr;
//
//import net.thevpc.nuts.expr.*;
//import net.thevpc.nuts.util.NFunction2;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.List;
//
//public abstract class BinCompareFctNode extends AbstractOp {
//    public BinCompareFctNode(String name, int precedence) {
//        super(name, precedence, NExprOpAssociativity.LEFT, NExprOpType.INFIX);
//    }
//
//    @Override
//    public Object eval(String name, List<NExprNode> args, NExprDeclarations e) {
//        Object a = args.get(0).eval(e).get();
//        Object b = args.get(1).eval(e).get();
//        NFunction2 f = e.findCommonInfixOp(NExprCommonOp.parse(getName()).get()
//                , a == null ? null : a.getClass()
//                , b == null ? null : b.getClass()
//        ).orNull();
//        if(f!=null){
//            return f.apply(a,b);
//        }
//        return null;
//    }
//
//    protected abstract boolean evalOrdinal(long a, long b);
//
//    protected abstract boolean evalFloat(double a, double b);
//
//    protected abstract boolean evalBigDecimal(BigDecimal a, BigDecimal b);
//
//    protected abstract boolean evalBigInteger(BigInteger a, BigInteger b);
//}
