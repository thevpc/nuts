package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface NExprNode {
    NOptional<Object> eval(NExprDeclarations context);

    NExprNodeType getType();

    List<NExprNode> getChildren();

    String getName();
}
