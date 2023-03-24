package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprVar;
import net.thevpc.nuts.expr.NExprVarDeclaration;

public class DefaultNExprVarDeclaration implements NExprVarDeclaration {
    private String name;
    private NExprVar impl;

    public DefaultNExprVarDeclaration(String name, NExprVar impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object get(NExprDeclarations context) {
        return impl.get(name, context);
    }

    @Override
    public Object set(Object value, NExprDeclarations context) {
        return impl.set(name, value,  context);
    }
}
