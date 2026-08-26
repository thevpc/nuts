package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NGetter;

import java.util.List;

/**
 * NExprOpNode interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprOpNode extends NExprNode {
    /**
     * Returns the operand.
     *
     * @param index index
     * @return get operand result
     */
    NExprNode getOperand(int index);

    /**
     * Operands.
     *
     * @return operands result
     */
    @NGetter
    List<NExprNode> operands();
    /**
     * Uniform name.
     *
     * @return uniform name result
     */
    @NGetter
    String uniformName();
}
