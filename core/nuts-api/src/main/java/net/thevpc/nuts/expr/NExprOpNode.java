package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NGetter;

import java.util.List;

public interface NExprOpNode extends NExprNode {
    NExprNode getOperand(int index);

    @NGetter
    List<NExprNode> operands();
    @NGetter
    String uniformName();
}
