package net.thevpc.nuts.expr;

import java.util.List;

public interface NExprFunction {
    String getName();
    Object eval(List<NExprNodeValue> args, NExprContext context);
}
