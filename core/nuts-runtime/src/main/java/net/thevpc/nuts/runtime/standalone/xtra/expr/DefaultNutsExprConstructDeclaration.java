package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.util.List;

public class DefaultNutsExprConstructDeclaration implements NutsExprConstructDeclaration {
    private String name;
    private NutsExprConstruct impl;

    public DefaultNutsExprConstructDeclaration(String name, NutsExprConstruct impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public Object eval(List<NutsExprNode> args, NutsExprDeclarations context) {
        return impl.eval(name, args, context);
    }

    @Override
    public String getName() {
        return name;
    }
}
