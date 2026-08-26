package net.thevpc.nuts.reflect;


/**
 * NReflectMappingStrategy interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NReflectMappingStrategy {
    /**
     * Copy.
     *
     * @param a a
     * @param b b
     * @param context context
     * @return copy result
     */
    boolean copy(Object a, Object b, NReflectMapper context);
    /**
     * Map to type.
     *
     * @param a a
     * @param fromType from type
     * @param toType to type
     * @param context context
     * @return map to type result
     */
    Object mapToType(Object a, NReflectType fromType, NReflectType toType, NReflectMapper context);
}
