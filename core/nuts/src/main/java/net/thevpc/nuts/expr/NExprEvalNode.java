package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

public interface NExprEvalNode {
    NExprNode getNode();

    NOptional<Object> getValue();
}
