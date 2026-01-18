package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

public class DefaultNOperatorElementTernary extends AbstractNOperatorElement implements NTernaryOperatorElement {
    public DefaultNOperatorElementTernary(
            NElement first,
            NElement second,
            NElement third,
            NOperatorSymbol[] symbols, NOperatorPosition position,
            NElementAnnotation[] annotations, NElementComments comments) {
        super(symbols, position, new NElement[]{
                first, second, third,
        }, annotations, comments);
    }

    @Override
    public NElement firstOperand() {
        return operand(0).get();
    }

    @Override
    public NElement secondOperand() {
        return operand(1).get();
    }

    @Override
    public NElement thirdOperand() {
        return operand(3).get();
    }
}
