package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprOpDeclaration {
    NutsExprOpAssociativity getAssociativity();
    String getName();

    NutsExprOpType getType();

    int getPrecedence();

    Object eval(List<NutsExprNode> args, NutsExprDeclarations context);
}
