package net.thevpc.nuts.expr;

/**
 * NExprVarWriter interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NExprVarWriter {
    /**
     * Sets the set.
     *
     * @param value value
     * @param context context
     */
    void set(Object value, NExprContext context);
}
