package net.thevpc.nuts.elem;

import net.thevpc.nuts.expr.NOperatorAssociativity;

public interface NExprElementReshaperBuilder {
    static NExprElementReshaperBuilder of() {
        return NElements.of().createExprElementReshaperBuilder(NExprElementReshaperType.EMPTY);
    }

    NExprElementReshaperBuilder addUnaryOperator(NOperatorSymbol op);

    NExprElementReshaperBuilder addBinaryOperator(NOperatorSymbol op, int precedence, NOperatorAssociativity assoc);

    NExprElementReshaperBuilder clearOperators();

    NExprElementReshaper build();
}
