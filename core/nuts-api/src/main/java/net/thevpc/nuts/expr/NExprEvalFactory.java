package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

/**
 * NExprEvalFactory interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprEvalFactory {
    /**
     * Returns the function.
     *
     * @param fctName fct name
     * @param parentContext parent context
     * @return get function result
     */
    NOptional<NExprFunction> getFunction(String fctName, NExprContext parentContext);

    /**
     * Returns the function.
     *
     * @param fctName fct name
     * @param args args
     * @param parentContext parent context
     * @return get function result
     */
    NOptional<NExprFunction> getFunction(String fctName, NExprEvalNode[] args, NExprContext parentContext);

    /**
     * Returns the operator.
     *
     * @param opName op name
     * @param type type
     * @param parentContext parent context
     * @return get operator result
     */
    NOptional<NExprOperator> getOperator(String opName, NFixity type, NExprContext parentContext);

    /**
     * Returns the var.
     *
     * @param varName var name
     * @param parentContext parent context
     * @return get var result
     */
    NOptional<NExprVar> getVar(String varName, NExprContext parentContext);

}
