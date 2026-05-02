package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;

public class NElementMapperMapEntry implements NElementMapper<Map.Entry> {

    @Override
    public NElement createElement(Map.Entry o, Type typeOfSrc, NElementFactoryContext context) {
        Map.Entry je = (Map.Entry) o;
        return NElement.ofObjectBuilder()
                .set("key", context.toElement(je.getKey()))
                .set("value", context.toElement(je.getValue()))
                .build();
    }

    @Override
    public Object toSimple(Map.Entry src, Type typeOfSrc, NElementFactoryContext context) {
        Map.Entry je = (Map.Entry) src;
        return new AbstractMap.SimpleEntry<>(
                context.toSimple(je.getKey(), null),
                context.toSimple(je.getValue(), null)
        );
    }

    @Override
    public Map.Entry createObject(NElement o, Type to, NElementFactoryContext context) {
        if (to instanceof ParameterizedType) {
            Type[] kvt = ((ParameterizedType) to).getActualTypeArguments();
            return new AbstractMap.SimpleEntry(
                    context.toObject(o.asObject().get().get("key").orNull(), kvt[0]),
                    context.toObject(o.asObject().get().get("value").orNull(), kvt[0])
            );
        }
        return new AbstractMap.SimpleEntry(
                context.toObject(o.asObject().get().get("key").orNull(), Object.class),
                context.toObject(o.asObject().get().get("value").orNull(), Object.class)
        );
    }

}
