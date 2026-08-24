package net.thevpc.nuts.expr;

/**
 * NExprVarReader interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NExprVarReader {
    /**
     * Returns the get.
     *
     * @param context context
     * @return get result
     */
    Object get(NExprContext context);
}
