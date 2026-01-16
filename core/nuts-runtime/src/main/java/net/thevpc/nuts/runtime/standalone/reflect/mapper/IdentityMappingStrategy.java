package net.thevpc.nuts.runtime.standalone.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapper;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectMappingStrategy;

class IdentityMappingStrategy implements NReflectMappingStrategy {
    public static final IdentityMappingStrategy IDENTITY_TYPE_MAPPER = new IdentityMappingStrategy();
    @Override
    public boolean copy(Object o, Object o2, NReflectMapper context) {
        return false;
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapper context) {
        return o;
    }
}
