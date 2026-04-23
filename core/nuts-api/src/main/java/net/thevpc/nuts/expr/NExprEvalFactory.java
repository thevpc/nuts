package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

public interface NExprEvalFactory {
    NOptional<NExprFct> getFunction(String fctName, NExprContext parentContext);

    NOptional<NExprFct> getFunction(String fctName, NExprEvalNode[] args, NExprContext parentContext);

    NOptional<NExprOp> getOperator(String opName, NExprOpType type, NExprContext parentContext);

    NOptional<NExprVar> getVar(String varName, NExprContext parentContext);

}
