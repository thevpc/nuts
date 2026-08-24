package net.thevpc.nuts.expr;

import java.util.function.Supplier;

/**
 * NExprMutableContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprMutableContext extends NExprContext {
    /**
     * Declare function.
     *
     * @param fctImpl fct impl
     * @return declare function result
     */
    NExprFunction declareFunction(NExprFunction fctImpl);

    /**
     * Declare function.
     *
     * @param name name
     * @param fctImpl fct impl
     * @return declare function result
     */
    NExprFunction declareFunction(String name, NExprCallHandler fctImpl);

    /**
     * Declare construct.
     *
     * @param constructImpl construct impl
     * @return declare construct result
     */
    NExprFunction declareConstruct(NExprFunction constructImpl);

    /**
     * Declare construct.
     *
     * @param name name
     * @param constructImpl construct impl
     * @return declare construct result
     */
    NExprFunction declareConstruct(String name, NExprCallHandler constructImpl);

    /**
     * Declare var.
     *
     * @param name name
     * @return declare var result
     */
    NExprVar declareVar(String name);

    /**
     * Declare constant.
     *
     * @param name name
     * @param value value
     * @return declare constant result
     */
    NExprVar declareConstant(String name, Object value);

    /**
     * Declare var.
     *
     * @param varImpl var impl
     * @return declare var result
     */
    NExprVar declareVar(NExprVar varImpl);

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
    NExprOperator declareOperator(String name, NFixity type, int precedence, NOperatorAssociativity associativity, NExprCallHandler impl);

    /**
     * Declare operator.
     *
     * @param name name
     * @param impl impl
     * @return declare operator result
     */
    NExprOperator declareOperator(String name, NExprCallHandler impl);

    /**
     * Declare operator.
     *
     * @param impl impl
     * @return declare operator result
     */
    NExprOperator declareOperator(NExprOperator impl);

    /**
     * Declare operator.
     *
     * @param name name
     * @param type type
     * @param impl impl
     * @return declare operator result
     */
    NExprOperator declareOperator(String name, NFixity type, NExprCallHandler impl);

    /**
     * Sets the var value.
     *
     * @param varName var name
     * @param value value
     * @return set var value result
     */
    NExprMutableContext setVarValue(String varName, Object value);

    /**
     * Returns the or declare var.
     *
     * @param name name
     * @param initialValue initial value
     * @return get or declare var result
     */
    NExprVar getOrDeclareVar(String name, Supplier<Object> initialValue);

    /**
     * Undeclare var.
     *
     * @param member member
     * @return undeclare var result
     */
    NExprMutableContext undeclareVar(NExprVar member);

    /**
     * Undeclare function.
     *
     * @param member member
     * @return undeclare function result
     */
    NExprMutableContext undeclareFunction(NExprFunction member);

    /**
     * Undeclare construct.
     *
     * @param member member
     * @return undeclare construct result
     */
    NExprMutableContext undeclareConstruct(NExprFunction member);

    /**
     * Undeclare operator.
     *
     * @param member member
     * @return undeclare operator result
     */
    NExprMutableContext undeclareOperator(NExprOperator member);

    /**
     * Removes the specified var.
     *
     * @param member member
     * @return remove var result
     */
    NExprMutableContext removeVar(NExprVar member);

    /**
     * Removes the specified function.
     *
     * @param member member
     * @return remove function result
     */
    NExprMutableContext removeFunction(NExprFunction member);

    /**
     * Removes the specified construct.
     *
     * @param member member
     * @return remove construct result
     */
    NExprMutableContext removeConstruct(NExprFunction member);

    /**
     * Removes the specified operator.
     *
     * @param member member
     * @return remove operator result
     */
    NExprMutableContext removeOperator(NExprOperator member);


    /**
     * Removes the specified var.
     *
     * @param name name
     * @return remove var result
     */
    NExprMutableContext removeVar(String name);

    /**
     * Removes the specified function.
     *
     * @param name name
     * @return remove function result
     */
    NExprMutableContext removeFunction(String name);

    /**
     * Removes the specified construct.
     *
     * @param name name
     * @return remove construct result
     */
    NExprMutableContext removeConstruct(String name);

    /**
     * Removes the specified operator.
     *
     * @param name name
     * @param type type
     * @return remove operator result
     */
    NExprMutableContext removeOperator(String name, NFixity type);
}
