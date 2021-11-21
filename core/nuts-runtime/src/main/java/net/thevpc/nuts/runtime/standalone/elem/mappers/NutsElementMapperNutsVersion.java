package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsVersion implements NutsElementMapper<NutsVersion> {

    @Override
    public Object destruct(NutsVersion src, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
            NutsSession ws = context.getSession();
            return src.formatter().setSession(ws).setNtf(true).format();
        } else {
            return src.toString();
        }
    }

    @Override
    public NutsElement createElement(NutsVersion o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
            NutsSession session = context.getSession();
//                NutsText n = ws.text().toText(ws.version().formatter(o).setNtf(true).format());
//                return ws.elem().forPrimitive().buildNutsString(n);
            return NutsElements.of(session).ofString(o.formatter().setNtf(true).format().toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public NutsVersion createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return NutsVersion.of(o.asPrimitive().getString(),session);
    }

}
