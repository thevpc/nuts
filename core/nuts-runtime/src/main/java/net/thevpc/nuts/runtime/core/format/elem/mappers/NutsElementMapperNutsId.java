package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsId implements NutsElementMapper<NutsId> {

    @Override
    public Object destruct(NutsId o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.elem().isNtf()) {
            return context.getSession().id().formatter(o).setNtf(true).format();
        } else {
            return o.toString();
        }
    }

    @Override
    public NutsElement createElement(NutsId o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.elem().isNtf()) {
//                NutsWorkspace ws = context.getSession().getWorkspace();
//                NutsText n = ws.text().toText(ws.id().formatter(o).setNtf(true).format());
//                return ws.elem().forPrimitive().buildNutsString(n);
            NutsSession ws = context.getSession();
            return ws.elem().forString(ws.id().formatter(o).setNtf(true).format().toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public NutsId createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        return context.getSession().id().parser().parse(o.asPrimitive().getString());
    }

}
