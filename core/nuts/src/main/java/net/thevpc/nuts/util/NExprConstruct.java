package net.thevpc.nuts.util;

import java.util.List;

public interface NExprConstruct {
    Object eval(String name, List<NExprNode> args, NExprDeclarations context);
}
