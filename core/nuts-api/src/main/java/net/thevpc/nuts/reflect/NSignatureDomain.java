package net.thevpc.nuts.reflect;

public interface NSignatureDomain<T> {
    boolean isArray(T type);

    T getComponentType(T type);

    String toSignatureString(T type);

    boolean isAssignableFrom(T a, T b);

    boolean isPrimitive(T a);


    boolean isInterface(T any);

    T[] getInterfaces(T any);

    T getSuperType(T any) ;


    T toBoxedType(T a);

    T toPrimitiveType(T a);

    /**
     * Returns 0 for exact match,
     * positive for compatible (higher = further away),
     * -1 or Integer.MAX_VALUE for no match.
     */
    default int getDistance(T expected, T actual){
        return -1;
    }
}
