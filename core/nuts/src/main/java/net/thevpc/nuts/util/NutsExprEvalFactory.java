package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

public interface NutsExprEvalFactory {
    NutsOptional<NutsExprFct> getFunction(String fctName, NutsExprDeclarations parentContext);

    NutsOptional<NutsExprFct> getFunction(String fctName, NutsExprEvalNode[] args, NutsExprDeclarations parentContext);

    NutsOptional<NutsExprOp> getOperator(String opName, NutsExprOpType type, NutsExprDeclarations parentContext);

    NutsOptional<NutsExprVar> getVar(String varName, NutsExprDeclarations parentContext);

}
