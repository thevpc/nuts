package net.thevpc.nuts.expr;

@FunctionalInterface
public interface NExprVarWriter {
    void set(Object value, NExprContext context);
}
