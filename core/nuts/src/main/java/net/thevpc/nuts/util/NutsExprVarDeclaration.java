package net.thevpc.nuts.util;

public interface NutsExprVarDeclaration {
    String getName();
    Object get(NutsExprDeclarations context);

    Object set(Object value, NutsExprDeclarations context);
}
