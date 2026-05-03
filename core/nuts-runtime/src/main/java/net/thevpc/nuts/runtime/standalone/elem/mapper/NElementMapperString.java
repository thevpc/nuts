package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperString implements NElementMapper<String> {

    @Override
    public Object toSimple(NElementSerializerContext<String> context) {
        return context.instance();
    }

    @Override
    public NElement toElement(NElementSerializerContext<String> context) {
        return NElement.ofString(String.valueOf(context.instance()));
    }

    @Override
    public String toObject(NElementDeserializerContext context) {
        return context.element().asStringValue().get();
    }
}
