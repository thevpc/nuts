package net.thevpc.nuts.util;

import java.util.List;

public interface NExprConstructDeclaration {
    String getName();

    Object eval(List<NExprNode> args, NExprDeclarations context);
}
