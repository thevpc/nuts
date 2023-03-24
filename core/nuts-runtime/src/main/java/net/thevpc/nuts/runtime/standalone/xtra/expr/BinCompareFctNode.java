package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.expr.NExprOpAssociativity;
import net.thevpc.nuts.expr.NExprOpType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public abstract class BinCompareFctNode extends AbstractOp {
    public BinCompareFctNode(String name, int precedence) {
        super(name, precedence, NExprOpAssociativity.LEFT, NExprOpType.INFIX);
    }

    @Override
    public Object eval(String name, List<NExprNode> args, NExprDeclarations e) {
        Object a = args.get(0).eval(e).get();
        Object b = args.get(1).eval(e).get();
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
