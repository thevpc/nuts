package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsObjectElement extends NutsElementMapperNutsElement {

    public NutsElementMapperNutsObjectElement() {
    }

    @Override
    public NutsObjectElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        o = super.createObject(o, typeOfResult, context);
        if (o.type() == NutsElementType.OBJECT) {
            return o.asObject().get(session);
        }
        return context.elem().ofObject().set("value", o).build();
    }
}
