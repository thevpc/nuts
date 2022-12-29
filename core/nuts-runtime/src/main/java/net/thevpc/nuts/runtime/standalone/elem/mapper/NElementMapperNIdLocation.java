package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.NIdLocation;

import java.lang.reflect.Type;
import java.util.Map;

public class NElementMapperNIdLocation implements NElementMapper<NIdLocation> {

    @Override
    public Object destruct(NIdLocation src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(src, null);
    }

    @Override
    public NElement createElement(NIdLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(o, null);
    }

    @Override
    public NIdLocation createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        Map builder = context.defaultElementToObject(o, Map.class);
        return new NIdLocation(
                (String) builder.get("url"),
                (String) builder.get("region"),
                (String) builder.get("classifier")
        );
    }

}
