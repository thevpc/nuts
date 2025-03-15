package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.elem.NObjectElement;

import java.lang.reflect.Type;

public class NElementMapperNObjectElement extends NElementMapperNElement {

    public NElementMapperNObjectElement() {
    }

    @Override
    public NObjectElement createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        o = super.createObject(o, typeOfResult, context);
        if (o.type() == NElementType.OBJECT) {
            return o.asObject().get();
        }
        return context.elem().ofObjectBuilder().set("value", o).build();
    }
}
