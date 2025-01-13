package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprFct;
import net.thevpc.nuts.expr.NExprFctDeclaration;
import net.thevpc.nuts.expr.NExprNodeValue;

import java.util.List;

public class DefaultNExprFctDeclaration implements NExprFctDeclaration {
    private String name;
    private NExprFct impl;

    public DefaultNExprFctDeclaration(String name, NExprFct impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public Object eval(List<NExprNodeValue> args, NExprDeclarations context) {
        return impl.eval(name, args, context);
    }

    @Override
    public String getName() {
        return name;
    }
}
