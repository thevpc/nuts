package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

import java.util.Arrays;
import java.util.List;

public class DefaultNOperatorElementUnary extends AbstractNOperatorElement implements NUnaryOperatorElement {
    public DefaultNOperatorElementUnary(NOperatorSymbol symbol, NOperatorPosition position, NElement first, List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics) {
        super(Arrays.asList(symbol), position, Arrays.asList(first), affixes,diagnostics);
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
