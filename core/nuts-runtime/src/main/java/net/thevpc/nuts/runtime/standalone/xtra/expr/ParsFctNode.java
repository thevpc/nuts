package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;
import net.thevpc.nuts.expr.*;

import java.util.List;

public class ParsFctNode extends AbstractOp {
    public ParsFctNode() {
        super("(", NExprOpPrecedence.PARS, NOperatorAssociativity.LEFT, NExprOpType.POSTFIX);
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations e) {
        throw new IllegalArgumentException("unable to evaluate");
    }
}
