package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

/**
 * NExprEvalNode interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprEvalNode {
    /**
     * Node.
     *
     * @return node result
     */
    NExprNode node();

    /**
     * Value.
     *
     * @return value result
     */
    NOptional<Object> value();
}
