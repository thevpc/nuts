package net.thevpc.nuts.elem;

public interface NExprElementReshaperBuilder {
    static NExprElementReshaperBuilder ofDefaultBuilder() {
        return NElements.of().createDefaultExprElementReshaperBuilder();
    }

    NExprElementReshaperBuilder addUnaryOperator(NOperatorSymbol op);

    NExprElementReshaperBuilder addBinaryOperator(NOperatorSymbol op, int precedence, NOperatorAssociativity assoc);

    NExprElementReshaperBuilder clearOperators();

    NExprElementReshaper build();
}
