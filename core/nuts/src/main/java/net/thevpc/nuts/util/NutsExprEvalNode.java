package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

public interface NutsExprEvalNode {
    NutsExprNode getNode();

    NutsOptional<Object> getValue();
}
