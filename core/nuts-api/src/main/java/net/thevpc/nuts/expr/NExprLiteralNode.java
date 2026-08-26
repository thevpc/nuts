package net.thevpc.nuts.expr;

/**
 * NExprLiteralNode interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprLiteralNode extends NExprNode {
    /**
     * Value.
     *
     * @return value result
     */
    Object value();
}
