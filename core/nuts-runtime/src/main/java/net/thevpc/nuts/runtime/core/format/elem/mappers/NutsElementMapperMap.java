package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFactoryService;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsObjectElement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class NutsElementMapperMap implements NutsElementMapper<Map> {

    private final DefaultNutsElementFactoryService defaultNutsElementFactoryService;

    public NutsElementMapperMap(DefaultNutsElementFactoryService defaultNutsElementFactoryService) {
        this.defaultNutsElementFactoryService = defaultNutsElementFactoryService;
    }

    @Override
    public Object destruct(Map src, Type typeOfSrc, NutsElementFactoryContext context) {
        Map je = (Map) src;
        Map<Object, Object> m = new LinkedHashMap<>();
        if (je != null) {
            for (Object e0 : je.entrySet()) {
                Map.Entry e = (Map.Entry) e0;
                Object k = context.destruct(e.getKey(), null);
                Object v = context.destruct(e.getValue(), null);
                m.put(k, v);
            }
        }
        return m;
    }

    @Override
    public NutsElement createElement(Map o, Type typeOfSrc, NutsElementFactoryContext context) {
        Map je = (Map) o;
        Map<NutsElement, NutsElement> m = new LinkedHashMap<>();
        if (je != null) {
            for (Object e0 : je.entrySet()) {
                Map.Entry e = (Map.Entry) e0;
                NutsElement k = context.objectToElement(e.getKey(), null);
                NutsElement v = context.objectToElement(e.getValue(), null);
                m.put(k, v);
            }
        }
        return new DefaultNutsObjectElement(m, context.getSession());
    }

    public Map fillObject(NutsElement o, Map all, Type elemType1, Type elemType2, Type to, NutsElementFactoryContext context) {
        if (o.type() == NutsElementType.OBJECT) {
            for (NutsElementEntry kv : o.asObject().children()) {
                NutsElement k = kv.getKey();
                NutsElement v = kv.getValue();
                all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
            }
        } else if (o.type() == NutsElementType.ARRAY) {
            for (NutsElement ee : o.asArray().children()) {
                NutsObjectElement kv = ee.asObject();
                NutsElement k = kv.get(context.element().forString("key"));
                NutsElement v = kv.get(context.element().forString("value"));
                all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
            }
        } else {
            throw new NutsUnsupportedEnumException(context.getSession(), o.type());
        }

        return all;
    }

    @Override
    public Map createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        Class cls = Map.class;
        Type elemType1 = null;//Object.class;
        Type elemType2 = null;//Object.class;
        if (to instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) to;
            Type rawType = pt.getRawType();
            if (rawType instanceof Class) {
                cls = (Class) rawType;
            }
            elemType1 = pt.getActualTypeArguments()[0];
            elemType2 = pt.getActualTypeArguments()[1];
        }
        if (cls == null) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.plain("class is null"));
        }
        switch (cls.getName()) {
            case "java.util.Map":
            case "java.util.LinkedHashMap": {
                return fillObject(o, new LinkedHashMap(o.asObject().size()), elemType1, elemType2, to, context);
            }
            case "java.util.HashMap": {
                return fillObject(o, new HashMap(o.asObject().size()), elemType1, elemType2, to, context);
            }
            case "java.util.SortedMap":
            case "java.util.NavigableMap": {
                return fillObject(o, new TreeMap(), elemType1, elemType2, to, context);
            }
            default: {
                return fillObject(o, (Map) defaultNutsElementFactoryService.getTypesRepository().getType(to).newInstance(), elemType1, elemType2, to, context);
            }
        }
    }

}
