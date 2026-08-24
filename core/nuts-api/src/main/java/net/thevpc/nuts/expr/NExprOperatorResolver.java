package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

/**
 * NExprOperatorResolver interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NExprOperatorResolver {
    /**
     * Returns the operator.
     *
     * @param opName op name
     * @param type type
     * @param args args
     * @param context context
     * @return get operator result
     */
    NOptional<NExprOperator> getOperator(String opName, NFixity type, NExprNodeValue[] args, NExprContext context) ;
}
