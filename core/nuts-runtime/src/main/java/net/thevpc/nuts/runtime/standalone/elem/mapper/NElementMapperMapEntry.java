package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;

public class NElementMapperMapEntry implements NElementMapper<Map.Entry> {

    @Override
    public NElement toElement(NElementSerializerContext<Map.Entry>  context) {
        Map.Entry je = context.instance();
        return NElement.ofObjectBuilder()
                .set("key", context.toElement(je.getKey()))
                .set("value", context.toElement(je.getValue()))
                .build();
    }

    @Override
    public Object toSimple(NElementSerializerContext<Map.Entry> context) {
        Map.Entry je = context.instance();
        return new AbstractMap.SimpleEntry<>(
                context.toSimple(je.getKey(), null),
                context.toSimple(je.getValue(), null)
        );
    }

    @Override
    public Map.Entry toObject(NElementDeserializerContext context) {
        Type to = context.instanceType();
        NElement element = context.element();
        if (to instanceof ParameterizedType) {
            Type[] kvt = ((ParameterizedType) to).getActualTypeArguments();
            return new AbstractMap.SimpleEntry(
                    context.toObject(element.asObject().get().get("key").orNull(), kvt[0]),
                    context.toObject(element.asObject().get().get("value").orNull(), kvt[0])
            );
        }
        return new AbstractMap.SimpleEntry(
                context.toObject(element.asObject().get().get("key").orNull(), Object.class),
                context.toObject(element.asObject().get().get("value").orNull(), Object.class)
        );
    }

}
