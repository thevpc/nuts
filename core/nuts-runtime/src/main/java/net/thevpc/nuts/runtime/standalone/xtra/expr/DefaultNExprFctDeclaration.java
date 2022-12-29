package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NExprDeclarations;
import net.thevpc.nuts.util.NExprFct;
import net.thevpc.nuts.util.NExprFctDeclaration;

import java.util.List;

public class DefaultNExprFctDeclaration implements NExprFctDeclaration {
    private String name;
    private NExprFct impl;

    public DefaultNExprFctDeclaration(String name, NExprFct impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public Object eval(List<Object> args, NExprDeclarations context) {
        return impl.eval(name, args, context);
    }

    @Override
    public String getName() {
        return name;
    }
}
