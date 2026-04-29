package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

public interface NExprNodeValue extends NExprNode {
    NExprNode node();
    NOptional<Object> value();
}
