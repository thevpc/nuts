package net.thevpc.nuts.runtime.standalone.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapper;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectMappingStrategy;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

class ArrayToArrayMappingStrategy implements NReflectMappingStrategy {
    private final Type to;
    private final Type componentType;
    private final Class componentTypeRaw;

    public ArrayToArrayMappingStrategy(Type to) {
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
    public boolean copy(Object o, Object o2, NReflectMapper context) {
        int len = Array.getLength(o);
        boolean changed = false;
        for (int i = 0; i < len; i++) {
            Object ov = Array.get(o2, i);
            Object nv = context.mapToType(Array.get(o, i), context.getRepository().getType(componentType));
            if (!context.getEqualizer().equals(ov, nv)) {
                Array.set(o2, i, nv);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapper context) {
        int len = Array.getLength(o);
        Object o2 = Array.newInstance(componentTypeRaw, len);
        copy(o, o2, context);
        return o2;
    }
}
