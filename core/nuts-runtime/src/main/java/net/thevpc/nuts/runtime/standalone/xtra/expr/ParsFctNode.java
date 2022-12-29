package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.util.List;

public class ParsFctNode extends AbstractOp {
    public ParsFctNode() {
        super("(", NExprOpPrecedence.PARS, NExprOpAssociativity.LEFT, NExprOpType.POSTFIX);
    }

    @Override
    public Object eval(String name, List<NExprNode> args, NExprDeclarations e) {
        throw new IllegalArgumentException("unable to evaluate");
    }
}
