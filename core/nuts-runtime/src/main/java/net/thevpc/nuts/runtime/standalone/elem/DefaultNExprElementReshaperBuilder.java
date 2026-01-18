package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultNExprElementReshaperBuilder implements NExprElementReshaperBuilder {
    private final Map<NOperatorSymbol, Integer> precedence = new HashMap<>();
    private final Map<NOperatorSymbol, NOperatorAssociativity> associativity = new HashMap<>();

    public DefaultNExprElementReshaperBuilder(Map<NOperatorSymbol, Integer> precedence, Map<NOperatorSymbol, NOperatorAssociativity> associativity) {
        this.precedence.putAll(precedence);
        this.associativity.putAll(associativity);
    }
    public DefaultNExprElementReshaperBuilder() {
    }

    @Override
    public NExprElementReshaperBuilder addUnaryOperator(NOperatorSymbol op) {
        precedence.put(op, 90);
        associativity.put(op, NOperatorAssociativity.RIGHT);
        return this;
    }

    @Override
    public NExprElementReshaperBuilder addBinaryOperator(NOperatorSymbol op, int precedence, NOperatorAssociativity assoc) {
        this.precedence.put(op, precedence);
        associativity.put(op, assoc);
        return this;
    }

    @Override
    public NExprElementReshaperBuilder clearOperators() {
        precedence.clear();
        associativity.clear();
        return this;
    }

    @Override
    public NExprElementReshaper build() {
        return new DefaultNExprElementReshaper(precedence, associativity);
    }
}
