package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.expr.NFixity;

import java.util.List;

public class DefaultNOperatorElementNary extends AbstractNOperatorElement implements NAryOperatorElement{
    public DefaultNOperatorElementNary(List<NElement> operands, List<NOperatorSymbol> symbols, NFixity fixity,
                                       List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics,NElementMetadata metadata) {
        super(symbols, fixity, operands, affixes, diagnostics,metadata);
    }
}
