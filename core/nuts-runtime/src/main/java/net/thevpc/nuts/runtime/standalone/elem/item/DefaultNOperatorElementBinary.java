package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

public class DefaultNOperatorElementBinary extends AbstractNOperatorElement implements NBinaryOperatorElement{
    public DefaultNOperatorElementBinary(NOperatorSymbol symbol, NOperatorPosition position, NElement first, NElement second, NElementAnnotation[] annotations, NElementComments comments) {
        super(symbol, position, first, second, annotations, comments);
    }
}
