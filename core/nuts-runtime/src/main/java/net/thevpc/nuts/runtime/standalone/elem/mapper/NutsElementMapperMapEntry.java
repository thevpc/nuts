package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;

public class NutsElementMapperMapEntry implements NutsElementMapper<Map.Entry> {

    @Override
    public NutsElement createElement(Map.Entry o, Type typeOfSrc, NutsElementFactoryContext context) {
        Map.Entry je = (Map.Entry) o;
        return context.elem().ofObject()
                .set("key", context.objectToElement(je.getKey(), null))
                .set("value", context.objectToElement(je.getValue(), null))
                .build();
    }

    @Override
    public Object destruct(Map.Entry src, Type typeOfSrc, NutsElementFactoryContext context) {
        Map.Entry je = (Map.Entry) src;
        return new AbstractMap.SimpleEntry<>(
                context.destruct(je.getKey(), null),
                context.destruct(je.getValue(), null)
        );
    }

    @Override
    public Map.Entry createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        if (to instanceof ParameterizedType) {
            Type[] kvt = ((ParameterizedType) to).getActualTypeArguments();
            return new AbstractMap.SimpleEntry(
                    context.elementToObject(o.asObject().get(context.elem().ofString("key")), kvt[0]),
                    context.elementToObject(o.asObject().get(context.elem().ofString("value")), kvt[0])
            );
        }
        return new AbstractMap.SimpleEntry(
                context.elementToObject(o.asObject().get(context.elem().ofString("key")), Object.class),
                context.elementToObject(o.asObject().get(context.elem().ofString("value")), Object.class)
        );
    }

}
