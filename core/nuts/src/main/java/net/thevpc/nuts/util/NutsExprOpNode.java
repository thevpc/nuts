package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprOpNode extends NutsExprNode{
    NutsExprNode getOperand(int index);

    List<NutsExprNode> getOperands();
}
