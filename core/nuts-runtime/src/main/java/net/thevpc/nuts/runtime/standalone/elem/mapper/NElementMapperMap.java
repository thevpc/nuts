package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNPairElement;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNObjectElement;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NUnsupportedEnumException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class NElementMapperMap implements NElementMapper<Map> {

    public NElementMapperMap() {
    }

    @Override
    public Object toSimple(NElementSerializerContext<Map> context) {
        Map je = context.instance();
        Map<Object, Object> m = new LinkedHashMap<>();
        if (je != null) {
            for (Object e0 : je.entrySet()) {
                Map.Entry e = (Map.Entry) e0;
                Object k = context.toSimple(e.getKey(), null);
                Object v = context.toSimple(e.getValue(), null);
                m.put(k, v);
            }
        }
        return m;
    }

    @Override
    public NElement toElement(NElementSerializerContext<Map> context) {
        Map je = (Map) context.instance();
        List<NElement> m = new ArrayList<>();
        if (je != null) {
            for (Object e0 : je.entrySet()) {
                Map.Entry e = (Map.Entry) e0;
                NElement k = context.toElement(e.getKey());
                if(!(e.getKey() instanceof NElement) && k.isString()){
                    k= NElement.ofNameOrString(k.asStringValue().get());
                }
                NElement v = context.toElement(e.getValue());
                m.add(new DefaultNPairElement(k, v));
            }
        }
        return new DefaultNObjectElement(null, null, m);
    }

    public Map fillObject(NElement o, Map all, Type elemType1, Type elemType2, Type to, NElementFactoryContext context) {
        if (o.isAnyObject()) {
            for (NElement ee : o.asObject().get().children()) {
                NPairElement kv = (NPairElement) ee;
                NElement k = kv.key();
                NElement v = kv.value();
                all.put(context.toObject(k, elemType1), context.toObject(v, elemType2));
            }
        } else if (o.isAnyArray()) {
            for (NElement ee : o.asArray().get().children()) {
                NPairElement kv = (NPairElement) ee;
                NElement k = kv.key();
                NElement v = kv.value();
                all.put(context.toObject(k, elemType1), context.toObject(v, elemType2));
            }
        } else {
            throw new NUnsupportedEnumException(o.type());
        }

        return all;
    }

    @Override
    public Map toObject(NElementDeserializerContext context) {
        Type to = context.instanceType();
        NElement element = context.element();
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
                return fillObject(element, new LinkedHashMap(element.asObject().get().size()), elemType1, elemType2, to, context);
            }
            case "java.util.HashMap": {
                return fillObject(element, new HashMap(element.asObject().get().size()), elemType1, elemType2, to, context);
            }
            case "java.util.SortedMap":
            case "java.util.NavigableMap": {
                return fillObject(element, new TreeMap(), elemType1, elemType2, to, context);
            }
            default: {
                return fillObject(element, (Map) context.getTypesRepository().getType(to).newInstance(), elemType1, elemType2, to, context);
            }
        }
    }

}
