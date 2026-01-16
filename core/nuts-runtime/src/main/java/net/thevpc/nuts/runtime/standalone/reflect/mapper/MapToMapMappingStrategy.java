package net.thevpc.nuts.runtime.standalone.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapper;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectMappingStrategy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

class MapToMapMappingStrategy implements NReflectMappingStrategy {
    private final Type to;
    private final Type keyType;
    private final Type valueType;

    public MapToMapMappingStrategy(Type to) {
        this.to = to;
        if (to instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) to).getActualTypeArguments();
            keyType = actualTypeArguments[0];
            valueType = actualTypeArguments[1];
        } else {
            throw new IllegalArgumentException("Unexpected");
        }
    }

    @Override
    public boolean copy(Object fromObj, Object toObj, NReflectMapper context) {
        Map<Object, Object> fromColl = ((Map) fromObj);
        Map<Object, Object> toColl = (Map) toObj;
        boolean changed = false;
        for (Map.Entry<Object, Object> e : fromColl.entrySet()) {
            Object k = context.mapToType(e.getKey(), context.getRepository().getType(keyType));
            Object v = context.mapToType(e.getValue(), context.getRepository().getType(valueType));
            if (toColl.containsKey(k)) {
                Object o = toColl.get(k);
                if (!context.getEqualizer().equals(o, v)) {
                    toColl.put(k, v);
                    changed = true;
                } else {
                    toColl.put(k, v);
                    changed = true;
                }
            } else {
                toColl.put(k, v);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapper context) {
        Map c = (Map) context.getRepository().getType(to).newInstance();
        copy(o, c, context);
        return c;
    }
}
