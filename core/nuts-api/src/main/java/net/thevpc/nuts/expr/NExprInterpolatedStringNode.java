package net.thevpc.nuts.expr;

/**
 * NExprInterpolatedStringNode interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprInterpolatedStringNode extends NExprNode {
    /**
     * Expression.
     *
     * @return expression result
     */
    String expression();
}
