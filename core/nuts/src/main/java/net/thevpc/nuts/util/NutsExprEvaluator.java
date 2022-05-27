package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;

import java.util.Collections;
import java.util.List;

public interface NutsExprEvaluator {
    default NutsOptional<NutsExprFct> getFunction(String fctName, Object[] args, NutsExprDeclarations context) {
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("function not found %s", fctName));
    }

    default NutsOptional<NutsExprConstruct> getConstruct(String constructName, NutsExprNode[] args, NutsExprDeclarations context) {
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("construct not found %s", constructName));
    }

    default NutsOptional<NutsExprOp> getOperator(String opName, NutsExprOpType type, NutsExprNode[] args, NutsExprDeclarations context) {
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("operator not found %s", opName));
    }

    default NutsOptional<NutsExprVar> getVar(String varName, NutsExprDeclarations context) {
        return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("var not found %s", varName));
    }

    default int[] getOperatorPrecedences(NutsExprDeclarations context) {
        return new int[0];
    }
}
