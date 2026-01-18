package net.thevpc.nuts.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;

import java.util.List;

public interface NExprOp {
    NOperatorAssociativity getAssociativity();

    NExprOpType getType();

    int getPrecedence();

    Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context);
}
