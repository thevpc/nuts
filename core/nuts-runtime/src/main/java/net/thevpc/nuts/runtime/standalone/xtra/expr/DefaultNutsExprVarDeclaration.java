package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsExprDeclarations;
import net.thevpc.nuts.util.NutsExprVar;
import net.thevpc.nuts.util.NutsExprVarDeclaration;

public class DefaultNutsExprVarDeclaration implements NutsExprVarDeclaration {
    private String name;
    private NutsExprVar impl;

    public DefaultNutsExprVarDeclaration(String name, NutsExprVar impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object get(NutsExprDeclarations context) {
        return impl.get(name, context);
    }

    @Override
    public Object set(Object value, NutsExprDeclarations context) {
        return impl.set(name, value,  context);
    }
}
