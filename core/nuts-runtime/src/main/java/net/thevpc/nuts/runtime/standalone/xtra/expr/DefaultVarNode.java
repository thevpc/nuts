package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsExpr;

import java.util.Collections;
import java.util.List;

public class DefaultVarNode implements NutsExpr.Node {
    private final String name;

    @Override
    public NutsExpr.NodeType getType() {
        return NutsExpr.NodeType.VARIABLE;
    }


    @Override
    public List<NutsExpr.Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultVarNode(String name) {
        this.name = name;
    }

    @Override
    public Object eval(NutsExpr context) {
        return context.getVar(name);
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
