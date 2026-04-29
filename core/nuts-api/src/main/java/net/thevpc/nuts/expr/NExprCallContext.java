package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface NExprCallContext {
    String name();

    List<NExprNodeValue> args();

    NOptional<NExprNodeValue> arg(int index);

    NExprContext context();

    NExprCallContextType contextType();

    NExprOpType operatorType();

    int operatorPrecedence();

    NOperatorAssociativity operatorAssociativity();
}
