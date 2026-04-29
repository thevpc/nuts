package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.expr.NExprRPI;

public interface NExprFunction {
    static NExprFunction of(String fctName, NExprCallHandler handler) {
        return NExprRPI.of().createFunction(fctName, handler);
    }

    String name();

    Object eval(NExprCallContext callContext);
}
