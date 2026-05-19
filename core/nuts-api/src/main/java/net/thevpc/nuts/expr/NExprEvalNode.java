package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

public interface NExprEvalNode {
    NExprNode node();

    NOptional<Object> value();
}
