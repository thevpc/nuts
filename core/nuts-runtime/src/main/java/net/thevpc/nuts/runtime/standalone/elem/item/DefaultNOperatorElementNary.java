package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

public class DefaultNOperatorElementNary extends AbstractNOperatorElement implements NAryOperatorElement{
    public DefaultNOperatorElementNary(NElement[] operands,NOperatorSymbol[] symbols, NOperatorPosition position, NElementAnnotation[] annotations, NElementComments comments) {
        super(symbols, position, operands, annotations, comments);
    }
}
