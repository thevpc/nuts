package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.artifact.NIdLocation;
import net.thevpc.nuts.elem.NElementSerializerContext;

import java.lang.reflect.Type;
import java.util.Map;

public class NElementMapperNIdLocation implements NElementMapper<NIdLocation> {

    @Override
    public Object toSimple(NElementSerializerContext<NIdLocation> context) {
        return context.defaultToSimple(context.instance(), null);
    }

    @Override
    public NElement toElement(NElementSerializerContext<NIdLocation> context) {
        return context.defaultCreateElement(context.instance(), null);
    }

    @Override
    public NIdLocation toObject(NElementDeserializerContext context) {
        Map builder = context.defaultToObject(context.element(), Map.class);
        return new NIdLocation(
                (String) builder.get("url"),
                (String) builder.get("region"),
                (String) builder.get("classifier")
        );
    }

}
