package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

import java.util.Arrays;
import java.util.List;

public class DefaultNOperatorElementUnary extends AbstractNOperatorElement implements NUnaryOperatorElement {
    public DefaultNOperatorElementUnary(NOperatorSymbol symbol, NOperatorPosition position, NElement first, List<NElementAnnotation> annotations, NElementComments comments, List<NElementDiagnostic> diagnostics) {
        super(Arrays.asList(symbol), position, Arrays.asList(first), annotations, comments,diagnostics);
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
