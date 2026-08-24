package net.thevpc.nuts.expr;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

/**
 * NExprResolver interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprResolver {
    /**
     * Returns the function.
     *
     * @param fctName fct name
     * @param args args
     * @param context context
     * @return get function result
     */
    default NOptional<NExprFunction> getFunction(String fctName, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("function not found %s", fctName));
    }

    /**
     * Returns the construct.
     *
     * @param constructName construct name
     * @param args args
     * @param context context
     * @return get construct result
     */
    default NOptional<NExprFunction> getConstruct(String constructName, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("construct not found %s", constructName));
    }

    /**
     * Returns the operator.
     *
     * @param opName op name
     * @param type type
     * @param args args
     * @param context context
     * @return get operator result
     */
    default NOptional<NExprOperator> getOperator(String opName, NFixity type, NExprNodeValue[] args, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("operator not found %s of type %s", opName,type));
    }

    /**
     * Returns the var.
     *
     * @param varName var name
     * @param context context
     * @return get var result
     */
    default NOptional<NExprVar> getVar(String varName, NExprContext context) {
        return NOptional.ofEmpty(() -> NMsg.ofC("var not found %s", varName));
    }
}
