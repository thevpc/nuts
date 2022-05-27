package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprNode {
    Object eval(NutsExprDeclarations context);

    NutsExprNodeType getType();

    List<NutsExprNode> getChildren();

    String getName();
}
