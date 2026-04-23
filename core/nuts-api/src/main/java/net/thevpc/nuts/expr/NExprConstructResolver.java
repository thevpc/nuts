package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

@FunctionalInterface
public interface NExprConstructResolver {
    NOptional<NExprConstruct> getConstruct(String constructName, NExprNodeValue[] args, NExprContext context) ;
}
