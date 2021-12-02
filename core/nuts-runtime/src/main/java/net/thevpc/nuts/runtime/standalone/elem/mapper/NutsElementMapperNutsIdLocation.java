package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.NutsIdLocation;

import java.lang.reflect.Type;
import java.util.Map;

public class NutsElementMapperNutsIdLocation implements NutsElementMapper<NutsIdLocation> {

    @Override
    public Object destruct(NutsIdLocation src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(src, null);
    }

    @Override
    public NutsElement createElement(NutsIdLocation o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(o, null);
    }

    @Override
    public NutsIdLocation createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        Map builder = context.defaultElementToObject(o, Map.class);
        return new NutsIdLocation(
                (String) builder.get("url"),
                (String) builder.get("region"),
                (String) builder.get("classifier")
        );
    }

}
