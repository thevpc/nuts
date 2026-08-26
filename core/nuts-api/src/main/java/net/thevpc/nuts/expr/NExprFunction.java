package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.rpi.NExprRPI;

/**
 * NExprFunction interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprFunction {
    /**
     * Creates a new instance of of.
     *
     * @param fctName fct name
     * @param handler handler
     * @return of result
     */
    static NExprFunction of(String fctName, NExprCallHandler handler) {
        return NExprRPI.of().createFunction(fctName, handler);
    }

    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Eval.
     *
     * @param callContext call context
     * @return eval result
     */
    Object eval(NExprCallContext callContext);
}
