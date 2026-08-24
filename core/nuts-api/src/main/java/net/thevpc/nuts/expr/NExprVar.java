package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.rpi.NExprRPI;

/**
 * NExprVar interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprVar extends NExprVarReader, NExprVarWriter {
    /**
     * Creates a new instance of of var.
     *
     * @param name name
     * @return of var result
     */
    static NExprVar ofVar(String name) {
        return NExprRPI.of().createVar(name, null);
    }

    /**
     * Creates a new instance of of var.
     *
     * @param name name
     * @param value value
     * @return of var result
     */
    static NExprVar ofVar(String name, Object value) {
        return NExprRPI.of().createVar(name, value);
    }

    /**
     * Creates a new instance of of var.
     *
     * @param name name
     * @param reader reader
     * @param writer writer
     * @return of var result
     */
    static NExprVar ofVar(String name, NExprVarReader reader, NExprVarWriter writer) {
        return NExprRPI.of().createVar(name, reader, writer);
    }

    /**
     * Creates a new instance of of lazy const.
     *
     * @param name name
     * @param reader reader
     * @return of lazy const result
     */
    static NExprVar ofLazyConst(String name, NExprVarReader reader) {
        return NExprRPI.of().createLazyConst(name, reader);
    }

    /**
     * Creates a new instance of of const.
     *
     * @param name name
     * @param value value
     * @return of const result
     */
    static NExprVar ofConst(String name, Object value) {
        return NExprRPI.of().createConst(name, value);
    }

    /**
     * Creates a new instance of of read only.
     *
     * @param name name
     * @param reader reader
     * @return of read only result
     */
    static NExprVar ofReadOnly(String name, NExprVarReader reader) {
        return NExprRPI.of().createReadOnlyVar(name, reader);
    }


    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Returns the get.
     *
     * @param context context
     * @return get result
     */
    Object get(NExprContext context);

    /**
     * Sets the set.
     *
     * @param value value
     * @param context context
     */
    void set(Object value, NExprContext context);

}
