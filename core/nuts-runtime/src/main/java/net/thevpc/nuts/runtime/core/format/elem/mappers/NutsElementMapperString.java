package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;

import java.lang.reflect.Type;

public class NutsElementMapperString implements NutsElementMapper<String> {

    @Override
    public Object destruct(String src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(String o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().forString(String.valueOf(o));
    }

    @Override
    public String createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        return o.asPrimitive().getString();
    }
}
