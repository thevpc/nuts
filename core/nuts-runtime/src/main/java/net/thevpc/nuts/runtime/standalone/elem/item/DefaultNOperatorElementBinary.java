package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.expr.NFixity;

import java.util.Arrays;
import java.util.List;

public class DefaultNOperatorElementBinary extends AbstractNOperatorElement implements NBinaryOperatorElement {
    public DefaultNOperatorElementBinary(NOperatorSymbol symbol, NFixity fixity, NElement first, NElement second,
                                         List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        super(Arrays.asList(symbol), fixity
                , Arrays.asList(first, second)
                , affixes, diagnostics,metadata
        );
    }

    @Override
    public NOperatorSymbol operatorSymbol() {
        return operatorSymbols().get(0);
    }

    @Override
    public NElement firstOperand() {
        return operands().get(0);
    }

    @Override
    public NElement secondOperand() {
        return operands().get(1);
    }
}
