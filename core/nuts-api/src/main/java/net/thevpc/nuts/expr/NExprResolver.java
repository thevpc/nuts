package net.thevpc.nuts.expr;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

public interface NExprResolver {
    default NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", fctName));
    }

    default NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", constructName));
    }

    default NOptional<NExprOperator> getOperator(String opName, NExprOpType type, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s of type %s", opName,type));
    }

    default NOptional<NExprVar> getVar(String varName, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("var not found %s", varName));
    }
}
