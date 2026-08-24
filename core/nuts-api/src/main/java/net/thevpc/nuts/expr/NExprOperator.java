package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.rpi.NExprRPI;

public interface NExprOperator {
    static NExprOperator of(String name, NFixity operatorFixity, int operatorPrecedence, NOperatorAssociativity associativity, NExprCallHandler handler) {
        return NExprRPI.of().createOperator(name, operatorFixity, operatorPrecedence, associativity, handler);
    }

    NOperatorAssociativity operatorAssociativity();

    String name();

    NFixity fixity();

    int operatorPrecedence();

    Object eval(NExprCallContext callContext);
}
