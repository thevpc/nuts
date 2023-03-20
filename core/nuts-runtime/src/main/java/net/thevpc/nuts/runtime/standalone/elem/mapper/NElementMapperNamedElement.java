package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class NElementMapperNamedElement implements NElementMapper<NElementEntry> {

    @Override
    public Object destruct(NElementEntry src, Type typeOfSrc, NElementFactoryContext context) {
        return new AbstractMap.SimpleEntry<Object, Object>(
                context.defaultDestruct(src.getKey(), NElement.class),
                context.defaultDestruct(src.getValue(), NElement.class)
        );
    }

    @Override
    public NElement createElement(NElementEntry o, Type typeOfSrc, NElementFactoryContext context) {
        NElementEntry je = (NElementEntry) o;
        Map<String, Object> m = new HashMap<>();
        m.put("key", je.getKey());
        m.put("value", je.getValue());
        return context.objectToElement(m,
                context.getReflectRepository().getParametrizedType(
                        Map.class, null, new Type[]{String.class, Object.class}
                ).getJavaType()
        );
    }

    @Override
    public NElementEntry createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        Type[] args = (typeOfResult instanceof ParameterizedType)
                ? (((ParameterizedType) typeOfResult).getActualTypeArguments())
                : new Type[]{Object.class, Object.class};
        Type mapType = context.getReflectRepository().getParametrizedType(
                Map.class, null, args
        ).getJavaType();
        Map map = (Map) context.elementToObject(o, mapType);
        return new DefaultNElementEntry(
                (NElement) map.get("key"),
                (NElement) map.get("value")
        );
    }

}
