package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

public class DefaultNOperatorElementUnary extends AbstractNOperatorElement implements NUnaryOperatorElement {
    public DefaultNOperatorElementUnary(NOperatorSymbol symbol, NOperatorPosition position, NElement first, NElementAnnotation[] annotations, NElementComments comments) {
        super(new NOperatorSymbol[]{symbol}, position, new NElement[]{first}, annotations, comments);
    }

    @Override
    public NOperatorSymbol operatorSymbol() {
        return operatorSymbols().get(0);
    }

    @Override
    public NElement operand() {
        return operands().get(0);
    }
}
