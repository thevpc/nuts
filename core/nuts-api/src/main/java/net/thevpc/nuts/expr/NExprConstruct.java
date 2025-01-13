package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprConstruct {
    Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context);
}
