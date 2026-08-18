package net.thevpc.nuts.elem;

import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.internal.rpi.NElementRPI;

public interface NExprElementReshaperBuilder {
    static NExprElementReshaperBuilder of() {
        return NElementRPI.of().createExprElementReshaperBuilder(NExprElementReshaperType.EMPTY);
    }

    NExprElementReshaperBuilder addUnaryOperator(NOperatorSymbol op);

    NExprElementReshaperBuilder addBinaryOperator(NOperatorSymbol op, int precedence, NOperatorAssociativity assoc);

    NExprElementReshaperBuilder clearOperators();

    NExprElementReshaper build();
}
