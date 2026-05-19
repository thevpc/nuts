package net.thevpc.nuts.expr;

public interface NExprIfNode extends NExprNode {
    NExprNode conditionNode();
    NExprNode trueNode();
    NExprNode falseNode();
}
