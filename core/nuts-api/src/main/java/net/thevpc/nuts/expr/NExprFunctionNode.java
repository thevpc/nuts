package net.thevpc.nuts.expr;

import java.util.List;

/**
 * NExprFunctionNode interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprFunctionNode extends NExprNode {
    /**
     * Returns the argument.
     *
     * @param index index
     * @return get argument result
     */
    NExprNode getArgument(int index);
    /**
     * Arguments.
     *
     * @return arguments result
     */
    List<NExprNode> arguments();
}
