package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

@FunctionalInterface
public interface NExprOperatorResolver {
    NOptional<NExprOperator> getOperator(String opName, NFixity type, NExprNodeValue[] args, NExprContext context) ;
}
