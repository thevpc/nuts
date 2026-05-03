package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperNPrimitiveElement implements NElementMapper<NPrimitiveElement> {

    public NElementMapperNPrimitiveElement() {
    }

    @Override
    public Object toSimple(NPrimitiveElement src, Type typeOfSrc, NElementFactoryContext context) {
        return src.asLiteral().asObject().orNull();
    }

    @Override
    public NElement createElement(NPrimitiveElement src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NPrimitiveElement createObject(NElementDeserializerContext context) {
        if (context.element().type().isAnyPrimitive()) {
            return context.element().asPrimitive().get();
        }
        return NElement.ofString(context.element().toString());
    }
}
