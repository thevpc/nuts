package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

import java.util.List;

public interface NutsExprNode {
    NutsOptional<Object> eval(NutsExprDeclarations context);

    NutsExprNodeType getType();

    List<NutsExprNode> getChildren();

    String getName();
}
