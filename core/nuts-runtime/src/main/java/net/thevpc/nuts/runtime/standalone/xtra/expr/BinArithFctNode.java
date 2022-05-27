package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public abstract class BinArithFctNode extends AbstractOp {
    public BinArithFctNode(String name, int precedence) {
        super(name, precedence, NutsExprOpAssociativity.LEFT, NutsExprOpType.INFIX);
    }

    @Override
    public Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations e) {
        Object a = args.get(0).eval(e);
        Object b = args.get(1).eval(e);
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
