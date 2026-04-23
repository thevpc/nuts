package net.thevpc.nuts.expr;

public interface NExprVarDeclaration {
    String getName();

    Object get(NExprContext context);

    Object set(Object value, NExprContext context);
    NExprVar asVar();
}
