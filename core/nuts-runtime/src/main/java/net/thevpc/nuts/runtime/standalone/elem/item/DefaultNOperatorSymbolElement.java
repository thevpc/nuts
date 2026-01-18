package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

public class DefaultNOperatorSymbolElement extends AbstractNElement implements NOperatorSymbolElement {
    private NOperatorSymbol symbol;

    public DefaultNOperatorSymbolElement(NOperatorSymbol op, NElementAnnotation[] annotations, NElementComments comments) {
        super(NElementType.OPERATOR_SYMBOL, annotations, comments);
        this.symbol = op;
    }

    @Override
    public NOperatorSymbol symbol() {
        return symbol;
    }

    @Override
    public String toString(boolean compact) {
        return symbol.lexeme();
    }
}
