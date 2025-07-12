package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperNObjectElement extends NElementMapperNElement {

    public NElementMapperNObjectElement() {
    }

    @Override
    public NObjectElement createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        o = super.createObject(o, typeOfResult, context);
        if (o.isAnyObject()) {
            return o.asObject().get();
        }
        return NElement.ofObjectBuilder().set("value", o).build();
    }
}
