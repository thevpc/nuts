package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.expr.NExprRPI;

public interface NExprOperator {
    static NExprOperator of(String name, NExprOpType operatorType, int operatorPrecedence, NOperatorAssociativity associativity,NExprCallHandler handler) {
        return NExprRPI.of().createOperator(name, operatorType, operatorPrecedence, associativity, handler);
    }

    NOperatorAssociativity operatorAssociativity();

    String name();

    NExprOpType operatorType();

    int operatorPrecedence();

    Object eval(NExprCallContext callContext);
}
