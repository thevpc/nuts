package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprVar;
import net.thevpc.nuts.expr.NExprVarReader;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIllegalArgumentException;

public class NExprVarConstFromFunction implements NExprVar {
    private NExprVarReader f;
    private Object loadedValue;
    private boolean loaded;
    private String name;

    public NExprVarConstFromFunction(String name, NExprVarReader f) {
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
        if(loaded){
            return loadedValue;
        }
        loadedValue = f.get(context);
        loaded = true;
        return loadedValue;
    }

    @Override
    public void set(Object value, NExprContext context) {
        throw new NIllegalArgumentException(NMsg.ofC("readonly variable %s",name));
    }
}
