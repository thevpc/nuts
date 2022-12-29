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
        NSession session = context.getSession();
        o = super.createObject(o, typeOfResult, context);
        if (o.type() == NElementType.ARRAY) {
            return o.asArray().get(session);
        }
        return context.elem().ofArray().add(o).build();
    }
}
