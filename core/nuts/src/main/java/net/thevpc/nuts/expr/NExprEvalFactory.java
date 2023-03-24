package net.thevpc.nuts.expr;

import net.thevpc.nuts.NOptional;

public interface NExprEvalFactory {
    NOptional<NExprFct> getFunction(String fctName, NExprDeclarations parentContext);

    NOptional<NExprFct> getFunction(String fctName, NExprEvalNode[] args, NExprDeclarations parentContext);

    NOptional<NExprOp> getOperator(String opName, NExprOpType type, NExprDeclarations parentContext);

    NOptional<NExprVar> getVar(String varName, NExprDeclarations parentContext);

}
