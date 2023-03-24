package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprConstructDeclaration {
    String getName();

    Object eval(List<NExprNode> args, NExprDeclarations context);
}
