package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperNObjectElement extends NElementMapperNElement {

    public NElementMapperNObjectElement() {
    }

    @Override
    public NObjectElement createObject(NElementDeserializerContext context) {
        NElement element = super.createObject(context);
        if (element.isAnyObject()) {
            return element.asObject().get();
        }
        return NElement.ofObjectBuilder().set("value", element).build();
    }
}
