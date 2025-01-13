package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprFct {
    Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context);
}
