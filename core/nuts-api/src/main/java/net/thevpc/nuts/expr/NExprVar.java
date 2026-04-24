package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.expr.NExprRPI;

public interface NExprVar extends NExprVarReader, NExprVarWriter {
    static NExprVar ofVar(String name) {
        return NExprRPI.of().createVar(name, null);
    }

    static NExprVar ofVar(String name, Object value) {
        return NExprRPI.of().createVar(name, value);
    }

    static NExprVar ofVar(String name, NExprVarReader reader, NExprVarWriter writer) {
        return NExprRPI.of().createVar(name, reader, writer);
    }

    static NExprVar ofLazyConst(String name, NExprVarReader reader) {
        return NExprRPI.of().createLazyConst(name, reader);
    }

    static NExprVar ofConst(String name, Object value) {
        return NExprRPI.of().createConst(name, value);
    }

    static NExprVar ofReadOnly(String name, NExprVarReader reader) {
        return NExprRPI.of().createReadOnlyVar(name, reader);
    }


    String getName();

    Object get(NExprContext context);

    void set(Object value, NExprContext context);

}
