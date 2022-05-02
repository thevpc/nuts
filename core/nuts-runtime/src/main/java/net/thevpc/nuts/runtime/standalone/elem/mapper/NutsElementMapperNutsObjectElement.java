package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementType;
import net.thevpc.nuts.elem.NutsObjectElement;

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
