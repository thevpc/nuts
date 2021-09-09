package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsVersion implements NutsElementMapper<NutsVersion> {

    @Override
    public Object destruct(NutsVersion src, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.element().isNtf()) {
            NutsWorkspace ws = context.getSession().getWorkspace();
            return ws.version().formatter(src).setNtf(true).format();
        } else {
            return src.toString();
        }
    }

    @Override
    public NutsElement createElement(NutsVersion o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.element().isNtf()) {
            NutsWorkspace ws = context.getSession().getWorkspace();
//                NutsText n = ws.text().toText(ws.version().formatter(o).setNtf(true).format());
//                return ws.elem().forPrimitive().buildNutsString(n);
            return ws.elem().forString(ws.version().formatter(o).setNtf(true).format().toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public NutsVersion createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        return context.getSession().getWorkspace().version().parser().parse(o.asPrimitive().getString());
    }

}
