package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprFctDeclaration {
    String getName();
    Object eval(List<Object> args, NutsExprDeclarations context);
}
