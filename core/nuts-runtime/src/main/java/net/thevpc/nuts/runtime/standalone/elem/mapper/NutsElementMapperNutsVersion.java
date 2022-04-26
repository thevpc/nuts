package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsVersion implements NutsElementMapper<NutsVersion> {

    @Override
    public Object destruct(NutsVersion src, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
            NutsSession ws = context.getSession();
            return src.formatter(context.getSession()).setSession(ws).setNtf(true).format();
        } else {
            return src.toString();
        }
    }

    @Override
    public NutsElement createElement(NutsVersion o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
            return context.elem().ofString(o.formatter(context.getSession()).setNtf(true).format().toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public NutsVersion createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return NutsVersion.of(o.asString().get(session)).get(session);
    }

}
