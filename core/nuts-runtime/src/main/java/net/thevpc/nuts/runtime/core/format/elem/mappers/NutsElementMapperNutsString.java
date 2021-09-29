package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.NutsString;

import java.lang.reflect.Type;

public class NutsElementMapperNutsString implements NutsElementMapper<NutsString> {

    @Override
    public Object destruct(NutsString src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.toString();
    }

    @Override
    public NutsElement createElement(NutsString o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NutsString createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        String i = context.defaultElementToObject(o, String.class);
        return context.getSession().text().parse(i);
    }
}
