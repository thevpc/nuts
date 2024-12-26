package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapperContext;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectTypeMapper;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Collection;

class CollectionToArrayTypeMapper implements NReflectTypeMapper {
    private final Type to;
    private final Type componentType;
    private final Class componentTypeRaw;

    public CollectionToArrayTypeMapper(Type to) {
        this.to = to;
        if (to instanceof GenericArrayType) {
            componentType = ((GenericArrayType) to).getGenericComponentType();
        } else if (to instanceof Class) {
            componentType = ((Class<?>) to).getComponentType();
        } else {
            throw new IllegalArgumentException("Unsupported");
        }
        componentTypeRaw = TypeHelper.rawClass(componentType).get();
    }

    @Override
    public boolean copy(Object fromObj, Object o2, NReflectMapperContext context) {
        Collection<Object> fromColl = ((Collection) fromObj);
        int i = 0;
        boolean changed = false;
        for (Object o : fromColl) {
            Object ov = Array.get(o2, i);
            Object nv = context.mapToType(o, componentType);
            if (!context.getEq().equals(ov, nv)) {
                Array.set(o2, i, nv);
                changed = true;
            }
            i++;
        }
        return changed;
    }

    @Override
    public Object mapToType(Object fromObj, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
        int len = ((Collection<?>) fromObj).size();
        Object o2 = Array.newInstance(componentTypeRaw, len);
        copy(fromObj, o2, context);
        return o2;
    }
}
