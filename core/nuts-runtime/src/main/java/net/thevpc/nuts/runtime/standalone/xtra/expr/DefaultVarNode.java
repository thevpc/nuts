package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsExprDeclarations;
import net.thevpc.nuts.util.NutsExprNode;
import net.thevpc.nuts.util.NutsExprNodeType;

import java.util.Collections;
import java.util.List;

public class DefaultVarNode implements NutsExprNode {
    private final String name;

    @Override
    public NutsExprNodeType getType() {
        return NutsExprNodeType.VARIABLE;
    }


    @Override
    public List<NutsExprNode> getChildren() {
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
    public Object eval(NutsExprDeclarations context) {
        return context.evalGetVar(name);
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
