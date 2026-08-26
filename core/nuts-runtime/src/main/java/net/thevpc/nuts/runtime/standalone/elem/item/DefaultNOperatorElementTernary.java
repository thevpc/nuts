package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.expr.NFixity;

import java.util.Arrays;
import java.util.List;

public class DefaultNOperatorElementTernary extends AbstractNOperatorElement implements NTernaryOperatorElement {
    public DefaultNOperatorElementTernary(
            NElement first,
            NElement second,
            NElement third,
            List<NOperatorSymbol> symbols, NFixity fixity,
            List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics,NElementMetadata metadata) {
        super(symbols, fixity, Arrays.asList(
                first, second, third
                ), affixes, diagnostics,metadata
        );
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
