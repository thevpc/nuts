package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.rpi.NExprRPI;

/**
 * NExprOperator interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprOperator {
    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param operatorFixity operator fixity
     * @param operatorPrecedence operator precedence
     * @param associativity associativity
     * @param handler handler
     * @return of result
     */
    static NExprOperator of(String name, NFixity operatorFixity, int operatorPrecedence, NOperatorAssociativity associativity, NExprCallHandler handler) {
        return NExprRPI.of().createOperator(name, operatorFixity, operatorPrecedence, associativity, handler);
    }

    /**
     * Operator associativity.
     *
     * @return operator associativity result
     */
    NOperatorAssociativity operatorAssociativity();

    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Fixity.
     *
     * @return fixity result
     */
    NFixity fixity();

    /**
     * Operator precedence.
     *
     * @return operator precedence result
     */
    int operatorPrecedence();

    /**
     * Eval.
     *
     * @param callContext call context
     * @return eval result
     */
    Object eval(NExprCallContext callContext);
}
