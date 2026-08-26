package net.thevpc.nuts.expr;

/**
 * NExprCallHandler interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprCallHandler {
    /**
     * Eval.
     *
     * @param callContext call context
     * @return eval result
     */
    Object eval(NExprCallContext callContext);
}
