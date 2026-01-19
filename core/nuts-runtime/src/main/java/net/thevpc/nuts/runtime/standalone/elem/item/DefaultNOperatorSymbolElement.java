package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

import java.util.List;

public class DefaultNOperatorSymbolElement extends AbstractNElement implements NOperatorSymbolElement {
    private NOperatorSymbol symbol;

    public DefaultNOperatorSymbolElement(NOperatorSymbol op) {
        this(op,null,null,null);
    }

    public DefaultNOperatorSymbolElement(NOperatorSymbol op, List<NElementAnnotation> annotations, NElementComments comments, List<NElementDiagnostic> diagnostics) {
        super(NElementType.OPERATOR_SYMBOL, annotations, comments, diagnostics);
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

    @Override
    public String toString() {
        return symbol.lexeme();
    }
}
