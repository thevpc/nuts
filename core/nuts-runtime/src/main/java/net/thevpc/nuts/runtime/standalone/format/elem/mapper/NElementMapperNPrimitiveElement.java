package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperNPrimitiveElement implements NElementMapper<NPrimitiveElement> {

    public NElementMapperNPrimitiveElement() {
    }

    @Override
    public Object destruct(NPrimitiveElement src, Type typeOfSrc, NElementFactoryContext context) {
        return src.asLiteral().asObject().orNull();
    }

    @Override
    public NElement createElement(NPrimitiveElement src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NPrimitiveElement createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        if (o.type().isPrimitive()) {
            return o.asPrimitive().get();
        }
        return NElements.ofString(o.toString());
    }
}
