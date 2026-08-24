package net.thevpc.nuts.reflect;

import java.util.List;

/**
 * NSignatureDomain interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NSignatureDomain<T> {
    /**
     * Checks if is array.
     *
     * @param type type
     * @return is array result
     */
    boolean isArray(T type);

    /**
     * Returns the component type.
     *
     * @param type type
     * @return get component type result
     */
    T getComponentType(T type);

    /**
     * Converts to signature string.
     *
     * @param type type
     * @return to signature string result
     */
    String toSignatureString(T type);

    /**
     * Checks if is assignable from.
     *
     * @param a a
     * @param b b
     * @return is assignable from result
     */
    boolean isAssignableFrom(T a, T b);

    /**
     * Checks if is primitive.
     *
     * @param a a
     * @return is primitive result
     */
    boolean isPrimitive(T a);


    /**
     * Checks if is interface.
     *
     * @param any any
     * @return is interface result
     */
    boolean isInterface(T any);

    /**
     * Returns the interfaces.
     *
     * @param any any
     * @return get interfaces result
     */
    List<T> getInterfaces(T any);

    /**
     * Returns the super type.
     *
     * @param any any
     * @return get super type result
     */
    T getSuperType(T any);


    /**
     * Converts to boxed type.
     *
     * @param a a
     * @return to boxed type result
     */
    T toBoxedType(T a);

    /**
     * Converts to primitive type.
     *
     * @param a a
     * @return to primitive type result
     */
    T toPrimitiveType(T a);

    /**
     * Returns 0 for exact match,
     * positive for compatible (higher = further away),
     * -1 or Integer.MAX_VALUE for no match.
     */
    default int getDistance(T expected, T actual) {
        return -1;
    }
}
