package net.thevpc.nuts.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;

import java.util.List;

public interface NExprOperator {
    NOperatorAssociativity getAssociativity();

    String getName();

    NExprOpType getType();

    int getPrecedence();

    Object eval(List<NExprNodeValue> args, NExprContext context);
}
