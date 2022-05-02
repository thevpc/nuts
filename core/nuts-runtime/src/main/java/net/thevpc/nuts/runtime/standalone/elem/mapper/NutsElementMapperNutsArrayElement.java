package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsArrayElement;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementType;

import java.lang.reflect.Type;

public class NutsElementMapperNutsArrayElement extends NutsElementMapperNutsElement {

    public NutsElementMapperNutsArrayElement() {
    }

    @Override
    public NutsArrayElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        o = super.createObject(o, typeOfResult, context);
        if (o.type() == NutsElementType.ARRAY) {
            return o.asArray().get(session);
        }
        return context.elem().ofArray().add(o).build();
    }
}
