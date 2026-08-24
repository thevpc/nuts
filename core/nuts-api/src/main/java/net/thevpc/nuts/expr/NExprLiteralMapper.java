package net.thevpc.nuts.expr;

/**
 * NExprLiteralMapper interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprLiteralMapper {
    /**
     * Map node.
     *
     * @param value value
     * @param context context
     * @return map node result
     */
    NExprNode mapNode(NExprNode value, NExprContext context);
}
