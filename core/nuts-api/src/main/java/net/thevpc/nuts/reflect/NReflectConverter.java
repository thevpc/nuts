package net.thevpc.nuts.reflect;

public interface NReflectConverter {
    Object convert(Object value, String path, NReflectType fromType, NReflectType toType, NReflectMapper context);
}
