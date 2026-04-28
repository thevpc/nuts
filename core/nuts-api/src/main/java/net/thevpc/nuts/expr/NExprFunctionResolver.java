package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

@FunctionalInterface
public interface NExprFunctionResolver {
    NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) ;
}
