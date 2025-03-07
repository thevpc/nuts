package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprVar;

public class ReservedNExprConst implements NExprVar {
    private final String name;
    private final Object value;

    public ReservedNExprConst(String name,Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Object get(String name, NExprDeclarations context) {
        return value;
    }

    @Override
    public Object set(String name, Object value, NExprDeclarations context) {
        return value;
    }
}
