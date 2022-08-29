package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprFunctionNode extends NutsExprNode{
    NutsExprNode getArgument(int index);
    List<NutsExprNode> getArguments();
}
