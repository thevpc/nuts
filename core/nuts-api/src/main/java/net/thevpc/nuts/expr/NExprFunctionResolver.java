package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

@FunctionalInterface
public interface NExprFunctionResolver {
    NOptional<NExprFct> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) ;
}
