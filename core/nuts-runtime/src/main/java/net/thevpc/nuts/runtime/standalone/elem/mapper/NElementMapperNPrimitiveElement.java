package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperNPrimitiveElement implements NElementMapper<NPrimitiveElement> {

    public NElementMapperNPrimitiveElement() {
    }

    @Override
    public Object toSimple(NElementSerializerContext<NPrimitiveElement> context) {
        return context.instance().asLiteral().asObject().orNull();
    }

    @Override
    public NElement toElement(NElementSerializerContext<NPrimitiveElement> context) {
        return context.instance();
    }

    @Override
    public NPrimitiveElement toObject(NElementDeserializerContext context) {
        if (context.element().type().isAnyPrimitive()) {
            return context.element().asPrimitive().get();
        }
        return NElement.ofString(context.element().toString());
    }
}
