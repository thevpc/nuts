package net.thevpc.nuts.expr;

public interface NExprContextBuilder {
    /**
     * creates a new context builder with empty settings.
     */
    static NExprContextBuilder of() {
        return NExprContext.of().childContext();
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

    NExprContextBuilder declareFunction(NExprFunction fctImpl);

    NExprContextBuilder declareConstruct(NExprFunction fctImpl);

    NExprContextBuilder declareOperator(NExprOperator fctImpl);

    NExprContextBuilder declareVar(NExprVar variable);

    NExprContextBuilder declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprCallHandler impl);

    NExprContextBuilder declareOperator(String name, NExprCallHandler impl);

    NExprContextBuilder declareOperator(String name, NExprOpType type, NExprCallHandler impl);

    NExprContextBuilder removeVar(String name);

    NExprContextBuilder removeFunction(String name);

    NExprContextBuilder removeConstruct(String name);

    NExprContextBuilder removeOperator(String name, NExprOpType type);

    NExprContextBuilder setAutoDeclareVariables(boolean autoDeclareVariables);

    NExprMutableContext buildMutable();

    NExprContextBuilder declareMathConstants();

    NExprContextBuilder declarePhysicsConstants();

    NExprContextBuilder declareMathFunctions();

    NExprContextBuilder declareBuiltins();
}
