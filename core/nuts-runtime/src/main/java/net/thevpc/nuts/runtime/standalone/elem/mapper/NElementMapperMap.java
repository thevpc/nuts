package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNObjectElement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class NElementMapperMap implements NElementMapper<Map> {

    private final DefaultNElementFactoryService defaultNutsElementFactoryService;

    public NElementMapperMap(DefaultNElementFactoryService defaultNutsElementFactoryService) {
        this.defaultNutsElementFactoryService = defaultNutsElementFactoryService;
    }

    @Override
    public Object destruct(Map src, Type typeOfSrc, NElementFactoryContext context) {
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
    public NElement createElement(Map o, Type typeOfSrc, NElementFactoryContext context) {
        Map je = (Map) o;
        List<NElementEntry> m = new ArrayList<>();
        if (je != null) {
            for (Object e0 : je.entrySet()) {
                Map.Entry e = (Map.Entry) e0;
                NElement k = context.objectToElement(e.getKey(), null);
                NElement v = context.objectToElement(e.getValue(), null);
                m.add(new DefaultNElementEntry(k, v));
            }
        }
        return new DefaultNObjectElement(m, context.getSession());
    }

    public Map fillObject(NElement o, Map all, Type elemType1, Type elemType2, Type to, NElementFactoryContext context) {
        NSession session = context.getSession();
        if (o.type() == NElementType.OBJECT) {
            for (NElementEntry kv : o.asObject().get(session).entries()) {
                NElement k = kv.getKey();
                NElement v = kv.getValue();
                all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
            }
        } else if (o.type() == NElementType.ARRAY) {
            for (NElement ee : o.asArray().get(session).items()) {
                NObjectElement kv = ee.asObject().get(session);
                NElement k = kv.get("key").orNull();
                NElement v = kv.get("value").orNull();
                all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
            }
        } else {
            throw new NUnsupportedEnumException(session, o.type());
        }

        return all;
    }

    @Override
    public Map createObject(NElement o, Type to, NElementFactoryContext context) {
        NSession session = context.getSession();
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
            throw new NIllegalArgumentException(session, NMsg.ofPlain("class is null"));
        }
        switch (cls.getName()) {
            case "java.util.Map":
            case "java.util.LinkedHashMap": {
                return fillObject(o, new LinkedHashMap(o.asObject().get(session).size()), elemType1, elemType2, to, context);
            }
            case "java.util.HashMap": {
                return fillObject(o, new HashMap(o.asObject().get(session).size()), elemType1, elemType2, to, context);
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
