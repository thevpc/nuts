package net.thevpc.nuts.expr;

import net.thevpc.nuts.reserved.ReservedNExprConst;
import net.thevpc.nuts.reserved.ReservedNExprVar;

public interface NExprVar {
    static NExprVar ofConst(Object value) {
        return new ReservedNExprConst(value);
    }

    static NExprVar of(Object value) {
        return new ReservedNExprVar(value);
    }

    Object get(String name, NExprDeclarations context);

    Object set(String name, Object value, NExprDeclarations context);

}
