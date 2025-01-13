package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprOp {
    NExprOpAssociativity getAssociativity();

    NExprOpType getType();

    int getPrecedence();

    Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context);
}
