package net.thevpc.nuts.elem;

public interface NExprElementReshaperBuilder {
    static NExprElementReshaperBuilder of() {
        return NElements.of().createExprElementReshaperBuilder(NExprElementReshaperType.EMPTY);
    }

    NExprElementReshaperBuilder addUnaryOperator(NOperatorSymbol op);

    NExprElementReshaperBuilder addBinaryOperator(NOperatorSymbol op, int precedence, NOperatorAssociativity assoc);

    NExprElementReshaperBuilder clearOperators();

    NExprElementReshaper build();
}
