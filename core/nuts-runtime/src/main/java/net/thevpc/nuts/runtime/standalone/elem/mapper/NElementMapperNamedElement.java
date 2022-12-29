package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementEntry;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.util.reflect.SimpleParametrizedType;
import net.thevpc.nuts.elem.DefaultNElementEntry;

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
        return context.objectToElement(m, ReflectUtils.createParametrizedType(Map.class, String.class, Object.class));
    }

    @Override
    public NElementEntry createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        Type[] args = (typeOfResult instanceof ParameterizedType)
                ? (((ParameterizedType) typeOfResult).getActualTypeArguments())
                : new Type[]{Object.class, Object.class};
        Type mapType = new SimpleParametrizedType(Map.class, args);
        Map map = (Map) context.elementToObject(o, mapType);
        return new DefaultNElementEntry(
                (NElement) map.get("key"),
                (NElement) map.get("value")
        );
    }

}
