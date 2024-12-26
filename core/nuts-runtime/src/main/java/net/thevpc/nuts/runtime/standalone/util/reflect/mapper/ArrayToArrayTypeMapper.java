package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapperContext;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectTypeMapper;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

class ArrayToArrayTypeMapper implements NReflectTypeMapper {
    private final Type to;
    private final Type componentType;
    private final Class componentTypeRaw;

    public ArrayToArrayTypeMapper(Type to) {
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
    public boolean copy(Object o, Object o2, NReflectMapperContext context) {
        int len = Array.getLength(o);
        boolean changed = false;
        for (int i = 0; i < len; i++) {
            Object ov = Array.get(o2, i);
            Object nv = context.mapToType(Array.get(o, i), context.repository().getType(componentType));
            if (!context.getEq().equals(ov, nv)) {
                Array.set(o2, i, nv);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
        int len = Array.getLength(o);
        Object o2 = Array.newInstance(componentTypeRaw, len);
        copy(o, o2, context);
        return o2;
    }
}
