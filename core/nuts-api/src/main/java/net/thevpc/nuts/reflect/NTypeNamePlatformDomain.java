package net.thevpc.nuts.reflect;

/**
 * NTypeNamePlatformDomain interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTypeNamePlatformDomain extends NTypeNameDomain {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTypeNamePlatformDomain of(){
        return NReflect.of().platformDomain();
    }

    /**
     * Returns the type class.
     *
     * @param any any
     * @return get type class result
     */
    <T> Class<T> getTypeClass(NTypeName<T> any);
}
