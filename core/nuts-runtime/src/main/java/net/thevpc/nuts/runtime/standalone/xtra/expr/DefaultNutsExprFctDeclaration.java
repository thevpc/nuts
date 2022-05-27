package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsExprDeclarations;
import net.thevpc.nuts.util.NutsExprFct;
import net.thevpc.nuts.util.NutsExprFctDeclaration;

import java.util.List;

public class DefaultNutsExprFctDeclaration implements NutsExprFctDeclaration {
    private String name;
    private NutsExprFct impl;

    public DefaultNutsExprFctDeclaration(String name, NutsExprFct impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public Object eval(List<Object> args, NutsExprDeclarations context) {
        return impl.eval(name, args, context);
    }

    @Override
    public String getName() {
        return name;
    }
}
