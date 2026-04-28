package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprFunctionHandler;
import net.thevpc.nuts.expr.NExprFunction;
import net.thevpc.nuts.expr.NExprNodeValue;

import java.util.List;

public class DefaultNExprFunction implements NExprFunction {
    private String name;
    private NExprFunctionHandler impl;

    public DefaultNExprFunction(String name, NExprFunctionHandler impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public Object eval(List<NExprNodeValue> args, NExprContext context) {
        return impl.eval(name, args, context);
    }

    @Override
    public String getName() {
        return name;
    }
}
