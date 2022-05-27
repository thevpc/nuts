package net.thevpc.nuts.util;

public interface NutsExprVar {
    static NutsExprVar ofConst(Object value) {
        return new ReservedNutsExprConst(value);
    }

    static NutsExprVar of(Object value) {
        return new ReservedNutsExprVar(value);
    }

    Object get(String name, NutsExprDeclarations context);

    Object set(String name, Object value, NutsExprDeclarations context);

}
