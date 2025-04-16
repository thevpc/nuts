package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.elem.NPrimitiveElement;

import java.lang.reflect.Type;

public class NElementMapperNPrimitiveElement implements NElementMapper<NPrimitiveElement> {

    public NElementMapperNPrimitiveElement() {
    }

    @Override
    public Object destruct(NPrimitiveElement src, Type typeOfSrc, NElementFactoryContext context) {
        return src.asLiteral().asRawObject();
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
        return context.elem().ofString(o.toString());
    }
}
