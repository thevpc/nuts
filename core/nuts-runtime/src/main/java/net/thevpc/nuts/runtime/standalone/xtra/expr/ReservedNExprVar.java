package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprVar;

public class ReservedNExprVar implements NExprVar {
    private String name;
    private Object value;

    public ReservedNExprVar(String name,Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Object get(String name, NExprContext context) {
        return value;
    }

    @Override
    public Object set(String name, Object value, NExprContext context) {
        Object old = this.value;
        this.value = value;
        return old;
    }
}
