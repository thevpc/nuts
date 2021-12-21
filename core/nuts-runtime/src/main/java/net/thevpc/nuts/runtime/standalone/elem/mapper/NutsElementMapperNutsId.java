package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsId implements NutsElementMapper<NutsId> {

    @Override
    public Object destruct(NutsId o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
            return o.formatter().setNtf(true).format();
        } else {
            return o.toString();
        }
    }

    @Override
    public NutsElement createElement(NutsId o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
//                NutsWorkspace ws = context.getSession().getWorkspace();
//                NutsText n = ws.text().toText(ws.id().formatter(o).setNtf(true).format());
//                return ws.elem().forPrimitive().buildNutsString(n);
            NutsSession session = context.getSession();
            return context.elem().ofString(o.formatter().setNtf(true).format().toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public NutsId createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return NutsId.of(o.asPrimitive().getString(),session);
    }

}
