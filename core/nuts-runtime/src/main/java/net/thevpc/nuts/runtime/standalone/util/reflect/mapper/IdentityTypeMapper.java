package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapperContext;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectTypeMapper;

class IdentityTypeMapper implements NReflectTypeMapper {
    public static final IdentityTypeMapper IDENTITY_TYPE_MAPPER = new IdentityTypeMapper();
    @Override
    public boolean copy(Object o, Object o2, NReflectMapperContext context) {
        return false;
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
        return o;
    }
}
