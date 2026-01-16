package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

public class DefaultNOperatorElementUnary extends AbstractNOperatorElement implements NUnaryOperatorElement{
    public DefaultNOperatorElementUnary(NOperatorSymbol symbol, NOperatorPosition position, NElement first, NElementAnnotation[] annotations, NElementComments comments) {
        super(symbol, position, first, null, annotations, comments);
    }
}
