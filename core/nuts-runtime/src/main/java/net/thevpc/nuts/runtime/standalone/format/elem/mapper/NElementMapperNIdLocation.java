package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

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
        return context.defaultCreateElement(o, null);
    }

    @Override
    public NIdLocation createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        Map builder = context.defaultCreateObject(o, Map.class);
        return new NIdLocation(
                (String) builder.get("url"),
                (String) builder.get("region"),
                (String) builder.get("classifier")
        );
    }

}
