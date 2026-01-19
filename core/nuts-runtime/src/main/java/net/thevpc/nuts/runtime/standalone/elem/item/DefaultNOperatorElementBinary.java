package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

import java.util.Arrays;
import java.util.List;

public class DefaultNOperatorElementBinary extends AbstractNOperatorElement implements NBinaryOperatorElement {
    public DefaultNOperatorElementBinary(NOperatorSymbol symbol, NOperatorPosition position, NElement first, NElement second, List<NElementAnnotation> annotations, NElementComments comments, List<NElementDiagnostic> diagnostics) {
        super(Arrays.asList(symbol), position
                , Arrays.asList(first, second)
                , annotations, comments, diagnostics
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
