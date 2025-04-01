package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;

public class DefaultNStringElement extends DefaultNPrimitiveElement implements NStringElement {

    public DefaultNStringElement(NElementType type, String value) {
        this(type, value, null, null);
    }

    public DefaultNStringElement(NElementType type, String value,
                                 NElementAnnotation[] annotations, NElementComments comments) {
        super(type, value, annotations, comments);
    }

    @Override
    public String stringValue() {
        return (String) value();
    }

}
