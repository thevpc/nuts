package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprOperatorHandler {
    NOperatorAssociativity qssociativity();

    NExprOpType operatorType();

    int operatorPrecedence();

    Object eval(String name, List<NExprNodeValue> args, NExprContext context);
}
