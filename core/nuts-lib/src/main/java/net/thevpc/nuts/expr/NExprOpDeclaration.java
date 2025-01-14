package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprOpDeclaration {
    NExprOpAssociativity getAssociativity();

    String getName();

    NExprOpType getType();

    int getPrecedence();

    Object eval(List<NExprNodeValue> args, NExprDeclarations context);
}
