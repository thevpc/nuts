package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.expr.NFixity;

import java.util.Arrays;
import java.util.List;

public class DefaultNOperatorElementUnary extends AbstractNOperatorElement implements NUnaryOperatorElement {
    public DefaultNOperatorElementUnary(NOperatorSymbol symbol, NFixity fixity, NElement first,
                                        List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        super(Arrays.asList(symbol), fixity, Arrays.asList(first), affixes,diagnostics,metadata);
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
