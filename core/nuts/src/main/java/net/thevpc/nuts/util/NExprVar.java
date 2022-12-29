package net.thevpc.nuts.util;

public interface NExprVar {
    static NExprVar ofConst(Object value) {
        return new ReservedNExprConst(value);
    }

    static NExprVar of(Object value) {
        return new ReservedNExprVar(value);
    }

    Object get(String name, NExprDeclarations context);

    Object set(String name, Object value, NExprDeclarations context);

}
