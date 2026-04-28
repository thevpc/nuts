package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

public interface NExprEvalFactory {
    NOptional<NExprFunction> getFunction(String fctName, NExprContext parentContext);

    NOptional<NExprFunction> getFunction(String fctName, NExprEvalNode[] args, NExprContext parentContext);

    NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprContext parentContext);

    NOptional<NExprVar> getVar(String varName, NExprContext parentContext);

}
