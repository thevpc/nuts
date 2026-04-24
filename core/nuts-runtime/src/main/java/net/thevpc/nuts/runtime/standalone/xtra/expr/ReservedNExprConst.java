package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprVar;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;

public class ReservedNExprConst implements NExprVar {
    private final String name;
    private final Object value;

    public ReservedNExprConst(String name, Object value) {
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
        throw new NIllegalArgumentException(NMsg.ofC("cannot set constant %s", name));
    }
}
