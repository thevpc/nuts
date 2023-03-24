package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprFunctionNode extends NExprNode {
    NExprNode getArgument(int index);
    List<NExprNode> getArguments();
}
