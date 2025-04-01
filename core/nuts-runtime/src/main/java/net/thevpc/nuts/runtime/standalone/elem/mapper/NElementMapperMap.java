package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNPairElement;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNObjectElement;
import net.thevpc.nuts.util.NMsg;

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
        List<NElement> m = new ArrayList<>();
        if (je != null) {
            for (Object e0 : je.entrySet()) {
                Map.Entry e = (Map.Entry) e0;
                NElement k = context.objectToElement(e.getKey(), null);
                if(!(e.getKey() instanceof NElement) && k.isString()){
                    k=context.elem().ofNameOrString(k.asString().get());
                }
                NElement v = context.objectToElement(e.getValue(), null);
                m.add(new DefaultNPairElement(k, v, new NElementAnnotation[0],null));
            }
        }
        return new DefaultNObjectElement(null, null, m, new NElementAnnotation[0],null);
    }

    public Map fillObject(NElement o, Map all, Type elemType1, Type elemType2, Type to, NElementFactoryContext context) {
        if (o.type() == NElementType.OBJECT) {
            for (NElement ee : o.asObject().get().children()) {
                NPairElement kv = (NPairElement) ee;
                NElement k = kv.key();
                NElement v = kv.value();
                all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
            }
        } else if (o.type() == NElementType.ARRAY) {
            for (NElement ee : o.asArray().get().items()) {
                NPairElement kv = (NPairElement) ee;
                NElement k = kv.key();
                NElement v = kv.value();
                all.put(context.elementToObject(k, elemType1), context.elementToObject(v, elemType2));
            }
        } else {
            throw new NUnsupportedEnumException(o.type());
        }

        return all;
    }

    @Override
    public Map createObject(NElement o, Type to, NElementFactoryContext context) {
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
            throw new NIllegalArgumentException(NMsg.ofPlain("class is null"));
        }
        switch (cls.getName()) {
            case "java.util.Map":
            case "java.util.LinkedHashMap": {
                return fillObject(o, new LinkedHashMap(o.asObject().get().size()), elemType1, elemType2, to, context);
            }
            case "java.util.HashMap": {
                return fillObject(o, new HashMap(o.asObject().get().size()), elemType1, elemType2, to, context);
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
