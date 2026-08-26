package net.thevpc.nuts.expr;

/**
 * NExprContextBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprContextBuilder {
    /**
     * creates a new context builder with empty settings.
     */
    static NExprContextBuilder of() {
        return NExprContext.of().childContext();
    }

    /**
     * Declare constructs.
     *
     * @param resolver resolver
     * @return declare constructs result
     */
    NExprContextBuilder declareConstructs(NExprFunctionResolver resolver);

    /**
     * Declare functions.
     *
     * @param resolver resolver
     * @return declare functions result
     */
    NExprContextBuilder declareFunctions(NExprFunctionResolver resolver);

    /**
     * Declare vars.
     *
     * @param resolver resolver
     * @return declare vars result
     */
    NExprContextBuilder declareVars(NExprVarResolver resolver);

    /**
     * Literal mapper.
     *
     * @return literal mapper result
     */
    NExprLiteralMapper  literalMapper();

    /**
     * Literal mapper.
     *
     * @param mapper mapper
     * @return literal mapper result
     */
    NExprContextBuilder  literalMapper(NExprLiteralMapper mapper);

    /**
     * Declare operators.
     *
     * @param resolver resolver
     * @return declare operators result
     */
    NExprContextBuilder declareOperators(NExprOperatorResolver resolver);

    /**
     * Declare resolver.
     *
     * @param resolver resolver
     * @return declare resolver result
     */
    NExprContextBuilder declareResolver(NExprResolver resolver);

    /**
     * Removes the specified constructs.
     *
     * @param resolver resolver
     * @return remove constructs result
     */
    NExprContextBuilder removeConstructs(NExprFunctionResolver resolver);

    /**
     * Removes the specified functions.
     *
     * @param resolver resolver
     * @return remove functions result
     */
    NExprContextBuilder removeFunctions(NExprFunctionResolver resolver);

    /**
     * Removes the specified vars.
     *
     * @param resolver resolver
     * @return remove vars result
     */
    NExprContextBuilder removeVars(NExprVarResolver resolver);

    /**
     * Removes the specified operators.
     *
     * @param resolver resolver
     * @return remove operators result
     */
    NExprContextBuilder removeOperators(NExprOperatorResolver resolver);

    /**
     * Removes the specified resolver.
     *
     * @param resolver resolver
     * @return remove resolver result
     */
    NExprContextBuilder removeResolver(NExprResolver resolver);

    /**
     * Build.
     *
     * @return build result
     */
    NExprContext build();

    /**
     * Declare function.
     *
     * @param fctImpl fct impl
     * @return declare function result
     */
    NExprContextBuilder declareFunction(NExprFunction fctImpl);

    /**
     * Declare construct.
     *
     * @param fctImpl fct impl
     * @return declare construct result
     */
    NExprContextBuilder declareConstruct(NExprFunction fctImpl);

    /**
     * Declare operator.
     *
     * @param fctImpl fct impl
     * @return declare operator result
     */
    NExprContextBuilder declareOperator(NExprOperator fctImpl);

    /**
     * Declare var.
     *
     * @param variable variable
     * @return declare var result
     */
    NExprContextBuilder declareVar(NExprVar variable);

    /**
     * Declare operator.
     *
     * @param name name
     * @param type type
     * @param precedence precedence
     * @param associativity associativity
     * @param impl impl
     * @return declare operator result
     */
    NExprContextBuilder declareOperator(String name, NFixity type, int precedence, NOperatorAssociativity associativity, NExprCallHandler impl);

    /**
     * Declare operator.
     *
     * @param name name
     * @param impl impl
     * @return declare operator result
     */
    NExprContextBuilder declareOperator(String name, NExprCallHandler impl);

    /**
     * Declare operator.
     *
     * @param name name
     * @param type type
     * @param impl impl
     * @return declare operator result
     */
    NExprContextBuilder declareOperator(String name, NFixity type, NExprCallHandler impl);

    /**
     * Removes the specified var.
     *
     * @param name name
     * @return remove var result
     */
    NExprContextBuilder removeVar(String name);

    /**
     * Removes the specified function.
     *
     * @param name name
     * @return remove function result
     */
    NExprContextBuilder removeFunction(String name);

    /**
     * Removes the specified construct.
     *
     * @param name name
     * @return remove construct result
     */
    NExprContextBuilder removeConstruct(String name);

    /**
     * Removes the specified operator.
     *
     * @param name name
     * @param type type
     * @return remove operator result
     */
    NExprContextBuilder removeOperator(String name, NFixity type);

    /**
     * Sets the auto declare variables.
     *
     * @param autoDeclareVariables auto declare variables
     * @return set auto declare variables result
     */
    NExprContextBuilder setAutoDeclareVariables(boolean autoDeclareVariables);

    /**
     * Build mutable.
     *
     * @return build mutable result
     */
    NExprMutableContext buildMutable();

    /**
     * Declare math constants.
     *
     * @return declare math constants result
     */
    NExprContextBuilder declareMathConstants();

    /**
     * Declare physics constants.
     *
     * @return declare physics constants result
     */
    NExprContextBuilder declarePhysicsConstants();

    /**
     * Declare math functions.
     *
     * @return declare math functions result
     */
    NExprContextBuilder declareMathFunctions();

    /**
     * Declare builtins.
     *
     * @return declare builtins result
     */
    NExprContextBuilder declareBuiltins();
}
