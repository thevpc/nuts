package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.NSession;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;

public class NElementMapperMapEntry implements NElementMapper<Map.Entry> {

    @Override
    public NElement createElement(Map.Entry o, Type typeOfSrc, NElementFactoryContext context) {
        Map.Entry je = (Map.Entry) o;
        return context.elem().ofObject()
                .set("key", context.objectToElement(je.getKey(), null))
                .set("value", context.objectToElement(je.getValue(), null))
                .build();
    }

    @Override
    public Object destruct(Map.Entry src, Type typeOfSrc, NElementFactoryContext context) {
        Map.Entry je = (Map.Entry) src;
        return new AbstractMap.SimpleEntry<>(
                context.destruct(je.getKey(), null),
                context.destruct(je.getValue(), null)
        );
    }

    @Override
    public Map.Entry createObject(NElement o, Type to, NElementFactoryContext context) {
        NSession session = context.getSession();
        if (to instanceof ParameterizedType) {
            Type[] kvt = ((ParameterizedType) to).getActualTypeArguments();
            return new AbstractMap.SimpleEntry(
                    context.elementToObject(o.asObject().get(session).get(context.elem().ofString("key")).orNull(), kvt[0]),
                    context.elementToObject(o.asObject().get(session).get(context.elem().ofString("value")).orNull(), kvt[0])
            );
        }
        return new AbstractMap.SimpleEntry(
                context.elementToObject(o.asObject().get(session).get(context.elem().ofString("key")).orNull(), Object.class),
                context.elementToObject(o.asObject().get(session).get(context.elem().ofString("value")).orNull(), Object.class)
        );
    }

}
