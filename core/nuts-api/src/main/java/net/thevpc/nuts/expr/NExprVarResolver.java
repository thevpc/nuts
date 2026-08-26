package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.rpi.NExprRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;
import java.util.function.Function;

/**
 * NExprVarResolver interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NExprVarResolver {
    /**
     * Creates a new instance of of const.
     *
     * @param vars vars
     * @return of const result
     */
    static NExprVarResolver ofConst(Function<String, Object> vars) {
        return NExprRPI.of().createLazyConstResolver(vars);
    }

    /**
     * Creates a new instance of of read only.
     *
     * @param vars vars
     * @return of read only result
     */
    static NExprVarResolver ofReadOnly(Function<String, Object> vars) {
        return NExprRPI.of().createReadOnlyVarResolver(vars);
    }

    /**
     * Creates a new instance of of map.
     *
     * @param variables variables
     * @return of map result
     */
    static NExprVarResolver ofMap(Map<String, Object> variables) {
        return NExprRPI.of().createMapVarResolver(variables);
    }

    /**
     * Creates a new instance of of read only map.
     *
     * @param variables variables
     * @return of read only map result
     */
    static NExprVarResolver ofReadOnlyMap(Map<String, Object> variables) {
        return NExprRPI.of().createMapVarResolver(variables);
    }

    /**
     * Returns the var.
     *
     * @param varName var name
     * @param context context
     * @return get var result
     */
    NOptional<NExprVar> getVar(String varName, NExprContext context);
}
