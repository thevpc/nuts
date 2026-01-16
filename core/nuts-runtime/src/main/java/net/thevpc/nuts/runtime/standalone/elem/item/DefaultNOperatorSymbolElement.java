package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

public class DefaultNOperatorSymbolElement extends AbstractNElement implements NOperatorSymbolElement {
    private NOperatorSymbol kind;

    public DefaultNOperatorSymbolElement(NOperatorSymbol op, NElementAnnotation[] annotations, NElementComments comments) {
        super(NElementType.OPERATOR_SYMBOL, annotations, comments);
        this.kind = op;
    }

    @Override
    public NOperatorSymbol kind() {
        return kind;
    }

    @Override
    public String toString(boolean compact) {
        return kind.lexeme();
    }
}
