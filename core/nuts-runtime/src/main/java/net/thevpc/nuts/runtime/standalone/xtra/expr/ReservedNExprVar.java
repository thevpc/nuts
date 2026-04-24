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
    public String getName() {
        return name;
    }

    @Override
    public Object get(NExprContext context) {
        return value;
    }

    @Override
    public void set(Object value, NExprContext context) {
        this.value = value;
    }
}
