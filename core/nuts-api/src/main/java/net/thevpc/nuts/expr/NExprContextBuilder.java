package net.thevpc.nuts.expr;

import net.thevpc.nuts.elem.NOperatorAssociativity;

import java.util.Map;
import java.util.function.Function;

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

    NExprContextBuilder declareConstructs(NExprConstructResolver resolver);

    NExprContextBuilder declareFunctions(NExprFunctionResolver resolver);

    NExprContextBuilder declareVars(NExprVarResolver resolver);

    NExprContextBuilder declareOperators(NExprOperatorResolver resolver);

    NExprContextBuilder declareResolver(NExprResolver resolver);

    NExprContextBuilder removeConstructs(NExprConstructResolver resolver);

    NExprContextBuilder removeFunctions(NExprFunctionResolver resolver);

    NExprContextBuilder removeVars(NExprVarResolver resolver);

    NExprContextBuilder removeOperators(NExprOperatorResolver resolver);

    NExprContextBuilder removeResolver(NExprResolver resolver);

    NExprContext build();

    NExprContextBuilder declareFunction(String name, NExprFct fctImpl);

    NExprContextBuilder declareConstruct(String name, NExprConstruct constructImpl);

    NExprContextBuilder declareVar(NExprVar variable);

    NExprContextBuilder declareOperator(String name, NExprOpType type, int precedence, NOperatorAssociativity associativity, NExprConstruct impl);

    NExprContextBuilder declareOperator(String name, NExprConstruct impl);

    NExprContextBuilder declareOperator(String name, NExprOpType type, NExprConstruct impl);

    NExprContextBuilder removeVar(String name);

    NExprContextBuilder removeFunction(String name);

    NExprContextBuilder removeConstruct(String name);

    NExprContextBuilder removeOperator(String name, NExprOpType type);

    NExprContextBuilder setAutoDeclareVariables(boolean autoDeclareVariables);

    NExprMutableContext buildMutable();

}
