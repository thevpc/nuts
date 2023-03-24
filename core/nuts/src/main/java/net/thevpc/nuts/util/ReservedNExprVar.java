package net.thevpc.nuts.util;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprVar;

public class ReservedNExprVar implements NExprVar {
    private Object value;

    public ReservedNExprVar(Object value) {
        this.value = value;
    }

    @Override
    public Object get(String name, NExprDeclarations context) {
        return value;
    }

    @Override
    public Object set(String name, Object value, NExprDeclarations context) {
        Object old = this.value;
        this.value = value;
        return old;
    }
}
