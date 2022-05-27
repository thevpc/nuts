package net.thevpc.nuts.util;

import java.util.List;

public interface NutsExprFct {
    Object eval(String name, List<Object> args, NutsExprDeclarations context);
}
