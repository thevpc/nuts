package net.thevpc.nuts.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;

public interface NExprContextBuilder {
    /**
     * creates a new context builder with default settings.
     * synonym for {@link #ofDefault()}
     *
     * @return ofDefault()
     */
    static NExprContextBuilder of() {
        return ofDefault();
    }

    static NExprContextBuilder ofDefault() {
        return NExprContext.ofDefault().childContext();
    }

    static NExprContextBuilder ofEmpty() {
        return NExprContext.ofEmpty().childContext();
    }

    NExprContextBuilder declareConstructs(NExprFunctionResolver resolver);

    NExprContextBuilder declareFunctions(NExprFunctionResolver resolver);

    NExprContextBuilder declareVars(NExprVarResolver resolver);

    NExprContextBuilder declareOperators(NExprOperatorResolver resolver);

    NExprContextBuilder declareResolver(NExprResolver resolver);

    NExprContextBuilder removeConstructs(NExprFunctionResolver resolver);

    NExprContextBuilder removeFunctions(NExprFunctionResolver resolver);

    NExprContextBuilder removeVars(NExprVarResolver resolver);

    NExprContextBuilder removeOperators(NExprOperatorResolver resolver);

    NExprContextBuilder removeResolver(NExprResolver resolver);

    NExprContext build();

    NExprContextBuilder declareFunction(String name, NExprFunctionHandler fctImpl);

    NExprContextBuilder declareConstruct(String name, NExprFunctionHandler constructImpl);

    NExprContextBuilder declareVar(NExprVar variable);

    NExprContextBuilder declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprFunctionHandler impl);

    NExprContextBuilder declareOperator(String name, NExprFunctionHandler impl);

    NExprContextBuilder declareOperator(String name, NExprOpType type, NExprFunctionHandler impl);

    NExprContextBuilder removeVar(String name);

    NExprContextBuilder removeFunction(String name);

    NExprContextBuilder removeConstruct(String name);

    NExprContextBuilder removeOperator(String name, NExprOpType type);

    NExprContextBuilder setAutoDeclareVariables(boolean autoDeclareVariables);

    NExprMutableContext buildMutable();

}
