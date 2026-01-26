package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNOperatorSymbolElementBuilder;

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

    public static NElement of(NOperatorSymbol operator) {
        return new  DefaultNOperatorSymbolElement(operator);
    }

    @Override
    public NOperatorSymbol symbol() {
        return symbol;
    }

    @Override
    public NOperatorSymbolElementBuilder builder() {
        return new DefaultNOperatorSymbolElementBuilder()
                .copyFrom(this);
    }
}
