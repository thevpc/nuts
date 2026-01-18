package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

public class DefaultNOperatorElementBinary extends AbstractNOperatorElement implements NBinaryOperatorElement {
    public DefaultNOperatorElementBinary(NOperatorSymbol symbol, NOperatorPosition position, NElement first, NElement second, NElementAnnotation[] annotations, NElementComments comments) {
        super(new NOperatorSymbol[]{symbol}, position
                , new NElement[]{first, second}
                , annotations, comments
        );
    }

    @Override
    public NOperatorSymbol operatorSymbol() {
        return operatorSymbols().get(0);
    }

    @Override
    public NElement firstOperand() {
        return operands().get(0);
    }

    @Override
    public NElement secondOperand() {
        return operands().get(1);
    }
}
