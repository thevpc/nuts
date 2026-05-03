package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperNArrayElement extends NElementMapperNElement {

    public NElementMapperNArrayElement() {
    }

    @Override
    public NArrayElement createObject(NElementDeserializerContext context) {
        NElement element = super.createObject(context);
        if (element.type() == NElementType.ARRAY) {
            return element.asArray().get();
        }
        return NElement.ofArrayBuilder().add(element).build();
    }
}
