package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprVar;
import net.thevpc.nuts.expr.NExprVarReader;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.function.Function;
import java.util.function.Supplier;

public class NExprVarReadOnlyFromFunction implements NExprVar {
    private final NExprVarReader f;
    private final String name;

    public NExprVarReadOnlyFromFunction(String name, NExprVarReader f) {
        NAssert.requireNamedNonBlank(name, "name");
        NAssert.requireNamedNonNull(f, "function");
        this.f = f;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object get(NExprContext context) {
        return f.get(context);
    }

    @Override
    public void set(Object value, NExprContext context) {
        throw new NIllegalArgumentException(NMsg.ofC("readonly variable %s", name));
    }
}
