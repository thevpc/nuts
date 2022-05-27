package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprConstruct {
    Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations context);
}
