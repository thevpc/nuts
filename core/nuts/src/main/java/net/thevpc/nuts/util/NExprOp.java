package net.thevpc.nuts.util;

import java.util.List;

public interface NExprOp {
    NExprOpAssociativity getAssociativity();

    NExprOpType getType();

    int getPrecedence();

    Object eval(String name, List<NExprNode> args, NExprDeclarations context);
}
