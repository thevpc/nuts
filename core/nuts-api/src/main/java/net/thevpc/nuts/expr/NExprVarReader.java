package net.thevpc.nuts.expr;

@FunctionalInterface
public interface NExprVarReader {
    Object get(NExprContext context);
}
