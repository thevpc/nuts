package net.thevpc.nuts.expr;

public interface NExprIfNode extends NExprNode {
    NExprNode getConditionNode();
    NExprNode getTrueNode();
    NExprNode getFalseNode();
}
