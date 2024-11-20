package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

public interface NExprEvaluator {
    default NOptional<NExprFct> getFunction(String fctName, Object[] args, NExprDeclarations context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", fctName));
    }

    default NOptional<NExprConstruct> getConstruct(String constructName, NExprNode[] args, NExprDeclarations context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", constructName));
    }

    default NOptional<NExprOp> getOperator(String opName, NExprOpType type, NExprNode[] args, NExprDeclarations context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s", opName));
    }

    default NOptional<NExprVar> getVar(String varName, NExprDeclarations context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("var not found %s", varName));
    }

    default int[] getOperatorPrecedences(NExprDeclarations context) {
        return new int[0];
    }
}
