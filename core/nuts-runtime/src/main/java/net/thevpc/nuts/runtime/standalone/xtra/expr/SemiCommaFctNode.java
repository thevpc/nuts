package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.expr.NExprOpAssociativity;
import net.thevpc.nuts.expr.NExprOpType;

import java.util.List;

public class SemiCommaFctNode extends AbstractOp {
    public SemiCommaFctNode(String name, int precedence) {
        super(name, precedence, NExprOpAssociativity.LEFT, NExprOpType.INFIX);
    }

    @Override
    public Object eval(String name, List<NExprNode> args, NExprDeclarations e) {
        Object a=null;
        for (NExprNode arg : args) {
            a = arg.eval(e).get();
        }
        return a;
    }
}