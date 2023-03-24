package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprOpNode extends NExprNode {
    NExprNode getOperand(int index);

    List<NExprNode> getOperands();
    String getUniformName();
}
