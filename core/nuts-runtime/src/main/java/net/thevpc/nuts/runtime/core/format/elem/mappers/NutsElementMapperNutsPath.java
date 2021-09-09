package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.NutsPath;

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
        return context.getSession().getWorkspace().io().path(i);
    }
}
