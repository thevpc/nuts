package net.thevpc.nuts.reflect;


public interface NReflectMappingStrategy {
    boolean copy(Object a, Object b, NReflectMapper context);
    Object mapToType(Object a, NReflectType fromType, NReflectType toType, NReflectMapper context);
}
