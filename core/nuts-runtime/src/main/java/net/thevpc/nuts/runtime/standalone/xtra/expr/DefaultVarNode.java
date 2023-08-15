package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.expr.NExprNodeType;
import net.thevpc.nuts.expr.NExprWordNode;

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
            return NOptional.ofError(x -> NMsg.ofC("error %s ", ex));
        }
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
