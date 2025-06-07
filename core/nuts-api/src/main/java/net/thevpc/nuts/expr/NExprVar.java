package net.thevpc.nuts.expr;

public interface NExprVar {

    Object get(String name, NExprDeclarations context);

    Object set(String name, Object value, NExprDeclarations context);

}
