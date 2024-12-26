package net.thevpc.nuts.reflect;


public interface NReflectTypeMapper {
    boolean copy(Object a, Object b, NReflectMapperContext context);
    Object mapToType(Object a, NReflectType fromType, NReflectType toType, NReflectMapperContext context);
}
