package net.thevpc.nuts.util;

import net.thevpc.nuts.NOptional;

public interface NExprEvalNode {
    NExprNode getNode();

    NOptional<Object> getValue();
}
