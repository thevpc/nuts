package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.util.NExprDeclarations;
import net.thevpc.nuts.util.NExprNode;
import net.thevpc.nuts.util.NExprNodeType;
import net.thevpc.nuts.util.NExprWordNode;

import java.util.Collections;
import java.util.List;

public class DefaultVarNode implements NExprWordNode {
    private final String name;

    @Override
    public NExprNodeType getType() {
        return NExprNodeType.WORD;
    }


    @Override
    public List<NExprNode> getChildren() {
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
    public NOptional<Object> eval(NExprDeclarations context) {
        try {
            return context.evalGetVar(name);
        } catch (Exception ex) {
            return NOptional.ofError(x -> NMsg.ofCstyle("error %s ", ex));
        }
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
