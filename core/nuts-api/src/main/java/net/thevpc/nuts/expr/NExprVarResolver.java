package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

@FunctionalInterface
public interface NExprVarResolver {
    NOptional<NExprVar> getVar(String varName, NExprContext context) ;
}
