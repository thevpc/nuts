package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprOp {
    NutsExprOpAssociativity getAssociativity();

    NutsExprOpType getType();

    int getPrecedence();

    Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations context);
}
