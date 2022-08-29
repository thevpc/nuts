package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.util.List;

public class ParsFctNode extends AbstractOp {
    public ParsFctNode() {
        super("(", NutsExprOpPrecedence.PARS, NutsExprOpAssociativity.LEFT, NutsExprOpType.POSTFIX);
    }

    @Override
    public Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations e) {
        throw new IllegalArgumentException("unable to evaluate");
    }
}
