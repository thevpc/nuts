package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsPrimitiveElement implements NutsElementMapper<NutsPrimitiveElement> {

    public NutsElementMapperNutsPrimitiveElement() {
    }

    @Override
    public Object destruct(NutsPrimitiveElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.getObject();
    }

    @Override
    public NutsElement createElement(NutsPrimitiveElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsPrimitiveElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        if (o.type().isPrimitive()) {
            return o.asPrimitive().get(session);
        }
        return context.elem().ofString(o.toString());
    }
}
