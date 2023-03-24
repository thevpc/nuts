package net.thevpc.nuts.expr;

public interface NExprVarDeclaration {
    String getName();
    Object get(NExprDeclarations context);

    Object set(Object value, NExprDeclarations context);
}
