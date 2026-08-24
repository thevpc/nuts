package net.thevpc.nuts.expr;

/**
 * NExprIfNode interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprIfNode extends NExprNode {
    /**
     * Condition node.
     *
     * @return condition node result
     */
    NExprNode conditionNode();
    /**
     * True node.
     *
     * @return true node result
     */
    NExprNode trueNode();
    /**
     * False node.
     *
     * @return false node result
     */
    NExprNode falseNode();
}
