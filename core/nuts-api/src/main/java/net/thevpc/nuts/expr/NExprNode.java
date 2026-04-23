package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface NExprNode {
    static NExprWordNode ofWord(String name) {
        return NExprs.of().newWord(name);
    }

    static NExprLiteralNode ofLiteral(Object name) {
        return NExprs.of().newLiteral(name);
    }

    NOptional<Object> eval(NExprContext context);

    NExprNodeType getType();

    List<NExprNode> getChildren();

    String getName();
}
