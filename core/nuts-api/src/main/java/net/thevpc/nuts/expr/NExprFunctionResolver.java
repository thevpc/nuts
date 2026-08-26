package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

/**
 * NExprFunctionResolver interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NExprFunctionResolver {
    /**
     * Returns the function.
     *
     * @param fctName fct name
     * @param args args
     * @param context context
     * @return get function result
     */
    NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) ;
}
