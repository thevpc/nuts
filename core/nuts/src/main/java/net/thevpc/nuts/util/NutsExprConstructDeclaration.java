package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprConstructDeclaration {
    String getName();

    Object eval(List<NutsExprNode> args, NutsExprDeclarations context);
}
