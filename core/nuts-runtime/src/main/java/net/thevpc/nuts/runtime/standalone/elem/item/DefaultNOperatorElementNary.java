package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

import java.util.List;

public class DefaultNOperatorElementNary extends AbstractNOperatorElement implements NAryOperatorElement{
    public DefaultNOperatorElementNary(List<NElement> operands, List<NOperatorSymbol> symbols, NOperatorPosition position, List<NElementAnnotation> annotations, NElementComments comments, List<NElementDiagnostic> diagnostics) {
        super(symbols, position, operands, annotations, comments, diagnostics);
    }
}
