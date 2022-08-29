package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.NutsExprDeclarations;
import net.thevpc.nuts.util.NutsExprNode;
import net.thevpc.nuts.util.NutsExprNodeType;
import net.thevpc.nuts.util.NutsExprWordNode;

import java.util.Collections;
import java.util.List;

public class DefaultVarNode implements NutsExprWordNode {
    private final String name;

    @Override
    public NutsExprNodeType getType() {
        return NutsExprNodeType.WORD;
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
    public NutsOptional<Object> eval(NutsExprDeclarations context) {
        try {
            return context.evalGetVar(name);
        } catch (Exception ex) {
            return NutsOptional.ofError(x -> NutsMessage.ofCstyle("error %s ", ex));
        }
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
