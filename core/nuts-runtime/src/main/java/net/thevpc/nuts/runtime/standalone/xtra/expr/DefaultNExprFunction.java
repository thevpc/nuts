package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.*;

import java.util.List;

public class DefaultNExprFunction implements NExprFunction {
    private String name;
    private NExprCallHandler impl;

    public DefaultNExprFunction(String name, NExprCallHandler impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public Object eval(NExprCallContext callContext) {
        return impl.eval(callContext);
    }

    @Override
    public String name() {
        return name;
    }
}
