package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprVar;

public class ReservedNExprVar implements NExprVar {
    private String name;
    private Object value;

    public ReservedNExprVar(String name,Object value) {
        this.name = name;
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
