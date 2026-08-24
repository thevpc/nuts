package net.thevpc.nuts.reflect;

/**
 * NReflectConverter interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NReflectConverter {
    /**
     * Convert.
     *
     * @param value value
     * @param path path
     * @param fromType from type
     * @param toType to type
     * @param context context
     * @return convert result
     */
    Object convert(Object value, String path, NReflectType fromType, NReflectType toType, NReflectMapper context);
}
