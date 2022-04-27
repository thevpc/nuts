package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsValue implements NutsElementMapper<NutsValue> {

    @Override
    public Object destruct(NutsValue src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(
                src.getRaw(), null
        );
    }

    @Override
    public NutsElement createElement(NutsValue o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(o.getRaw(), null);
    }

    @Override
    public NutsValue createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        Object any = context.defaultElementToObject(o, Object.class);
        return NutsValue.of(any);
    }

}
