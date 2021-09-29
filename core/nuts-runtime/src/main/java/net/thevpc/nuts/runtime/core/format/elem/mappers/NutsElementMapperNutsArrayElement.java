package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;
import java.util.stream.Collectors;

public class NutsElementMapperNutsArrayElement implements NutsElementMapper<NutsArrayElement> {

    public NutsElementMapperNutsArrayElement() {
    }

    @Override
    public Object destruct(NutsArrayElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.children().stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
    }

    @Override
    public NutsElement createElement(NutsArrayElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsArrayElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        if (o.type() == NutsElementType.ARRAY) {
            return o.asArray();
        }
        return context.elem().forArray().add(o).build();
    }
}
