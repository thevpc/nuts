package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

public class NElementMapperNArrayElement extends NElementMapperNElement {

    public NElementMapperNArrayElement() {
    }

    @Override
    public NArrayElement toObject(NElementDeserializerContext context) {
        NElement element = super.toObject(context);
        if (element.type() == NElementType.ARRAY) {
            return element.asArray().get();
        }
        return NElement.ofArrayBuilder().add(element).build();
    }
}
