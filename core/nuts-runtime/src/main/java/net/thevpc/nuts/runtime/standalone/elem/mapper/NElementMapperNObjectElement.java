package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

public class NElementMapperNObjectElement extends NElementMapperNElement {

    public NElementMapperNObjectElement() {
    }

    @Override
    public NObjectElement toObject(NElementDeserializerContext context) {
        NElement element = super.toObject(context);
        if (element.isAnyObject()) {
            return element.asObject().get();
        }
        return NElement.ofObjectBuilder().set("value", element).build();
    }
}
