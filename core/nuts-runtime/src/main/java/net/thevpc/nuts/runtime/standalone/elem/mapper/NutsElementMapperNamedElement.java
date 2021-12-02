package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementEntry;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.util.reflect.SimpleParametrizedType;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementEntry;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class NutsElementMapperNamedElement implements NutsElementMapper<NutsElementEntry> {

    @Override
    public Object destruct(NutsElementEntry src, Type typeOfSrc, NutsElementFactoryContext context) {
        return new AbstractMap.SimpleEntry<Object, Object>(
                context.defaultDestruct(src.getKey(), NutsElement.class),
                context.defaultDestruct(src.getValue(), NutsElement.class)
        );
    }

    @Override
    public NutsElement createElement(NutsElementEntry o, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsElementEntry je = (NutsElementEntry) o;
        Map<String, Object> m = new HashMap<>();
        m.put("key", je.getKey());
        m.put("value", je.getValue());
        return context.objectToElement(m, ReflectUtils.createParametrizedType(Map.class, String.class, Object.class));
    }

    @Override
    public NutsElementEntry createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        Type[] args = (typeOfResult instanceof ParameterizedType)
                ? (((ParameterizedType) typeOfResult).getActualTypeArguments())
                : new Type[]{Object.class, Object.class};
        Type mapType = new SimpleParametrizedType(Map.class, args);
        Map map = (Map) context.elementToObject(o, mapType);
        return new DefaultNutsElementEntry(
                (NutsElement) map.get("key"),
                (NutsElement) map.get("value")
        );
    }

}
