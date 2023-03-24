package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprConstruct;
import net.thevpc.nuts.expr.NExprConstructDeclaration;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNode;

import java.util.List;

public class DefaultNExprConstructDeclaration implements NExprConstructDeclaration {
    private String name;
    private NExprConstruct impl;

    public DefaultNExprConstructDeclaration(String name, NExprConstruct impl) {
        this.name = name;
        this.impl = impl;
    }

    @Override
    public Object eval(List<NExprNode> args, NExprDeclarations context) {
        return impl.eval(name, args, context);
    }

    @Override
    public String getName() {
        return name;
    }
}
