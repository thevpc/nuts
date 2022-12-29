package net.thevpc.nuts.util;

import java.util.List;

public interface NExprFctDeclaration {
    String getName();
    Object eval(List<Object> args, NExprDeclarations context);
}
