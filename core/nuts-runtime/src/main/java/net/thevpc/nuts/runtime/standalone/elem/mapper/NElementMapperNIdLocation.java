package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.artifact.NIdLocation;

import java.lang.reflect.Type;
import java.util.Map;

public class NElementMapperNIdLocation implements NElementMapper<NIdLocation> {

    @Override
    public Object toSimple(NIdLocation src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(src, null);
    }

    @Override
    public NElement createElement(NIdLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(o, null);
    }

    @Override
    public NIdLocation createObject(NElementDeserializerContext context) {
        Map builder = context.defaultToObject(context.element(), Map.class);
        return new NIdLocation(
                (String) builder.get("url"),
                (String) builder.get("region"),
                (String) builder.get("classifier")
        );
    }

}
