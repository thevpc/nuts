package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementType;

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
        return context.elem().ofArrayBuilder().add(o).build();
    }
}
