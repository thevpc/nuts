package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

import java.util.List;

public class DefaultNOperatorSymbolElement extends AbstractNElement implements NOperatorSymbolElement {
    private NOperatorSymbol symbol;

    public DefaultNOperatorSymbolElement(NOperatorSymbol op) {
        this(op,null,null);
    }

    public DefaultNOperatorSymbolElement(NOperatorSymbol op, List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics) {
        super(NElementType.OPERATOR_SYMBOL, affixes, diagnostics);
        this.symbol = op;
    }

    @Override
    public NOperatorSymbol symbol() {
        return symbol;
    }
}
