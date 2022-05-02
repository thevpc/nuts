package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.io.NutsPath;

import java.lang.reflect.Type;

public class NutsElementMapperNutsPath implements NutsElementMapper<NutsPath> {

    @Override
    public Object destruct(NutsPath src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.toString();
    }

    @Override
    public NutsElement createElement(NutsPath o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NutsPath createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        String i = context.defaultElementToObject(o, String.class);
        return NutsPath.of(i,context.getSession());
    }
}
