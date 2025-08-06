package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperNArrayElement extends NElementMapperNElement {

    public NElementMapperNArrayElement() {
    }

    @Override
    public NArrayElement createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        o = super.createObject(o, typeOfResult, context);
        if (o.type() == NElementType.ARRAY) {
            return o.asArray().get();
        }
        return NElement.ofArrayBuilder().add(o).build();
    }
}
