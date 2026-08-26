package net.thevpc.nuts.elem;

import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.internal.rpi.NElementRPI;

/**
 * NExprElementReshaperBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprElementReshaperBuilder {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NExprElementReshaperBuilder of() {
        return NElementRPI.of().createExprElementReshaperBuilder(NExprElementReshaperType.EMPTY);
    }

    /**
     * Adds the specified unary operator.
     *
     * @param op op
     * @return add unary operator result
     */
    NExprElementReshaperBuilder addUnaryOperator(NOperatorSymbol op);

    /**
     * Adds the specified binary operator.
     *
     * @param op op
     * @param precedence precedence
     * @param assoc assoc
     * @return add binary operator result
     */
    NExprElementReshaperBuilder addBinaryOperator(NOperatorSymbol op, int precedence, NOperatorAssociativity assoc);

    /**
     * Clear operators.
     *
     * @return clear operators result
     */
    NExprElementReshaperBuilder clearOperators();

    /**
     * Build.
     *
     * @return build result
     */
    NExprElementReshaper build();
}
