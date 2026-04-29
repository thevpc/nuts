package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface NExprNode {
    static NExprWordNode ofWord(String name) {
        return NExprRPI.of().createExprWordNode(name);
    }

    static NExprLiteralNode ofLiteral(Object name) {
        return NExprRPI.of().createExprLiteralNode(name);
    }

    NOptional<Object> eval(NExprContext context);

    NExprNodeType nodeType();

    List<NExprNode> children();

    String name();
}
