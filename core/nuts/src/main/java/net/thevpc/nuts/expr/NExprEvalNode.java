package net.thevpc.nuts.expr;

import net.thevpc.nuts.NOptional;

public interface NExprEvalNode {
    NExprNode getNode();

    NOptional<Object> getValue();
}
