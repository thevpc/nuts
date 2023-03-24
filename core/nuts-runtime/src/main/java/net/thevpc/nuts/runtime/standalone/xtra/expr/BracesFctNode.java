package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;

import java.util.List;

public class BracesFctNode extends AbstractOp {
    public BracesFctNode() {
        super("{", NExprOpPrecedence.BRACES, NExprOpAssociativity.LEFT, NExprOpType.POSTFIX);
    }

    @Override
    public Object eval(String name, List<NExprNode> args, NExprDeclarations e) {
        throw new IllegalArgumentException("unable to evaluate");
    }
}
