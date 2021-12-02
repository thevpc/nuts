package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsArrayElement extends NutsElementMapperNutsElement {

    public NutsElementMapperNutsArrayElement() {
    }

    @Override
    public NutsArrayElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        o = super.createObject(o, typeOfResult, context);
        if (o.type() == NutsElementType.ARRAY) {
            return o.asArray();
        }
        return context.elem().ofArray().add(o).build();
    }
}
