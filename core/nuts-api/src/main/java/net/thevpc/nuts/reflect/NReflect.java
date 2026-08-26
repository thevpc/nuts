package net.thevpc.nuts.reflect;

import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.lang.reflect.Type;

/**
 * NReflect interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NReflect extends NComponent {
    /**
     * Creates a new instance of.
     *
     * @return of result
     */
    static NReflect of() {
        return NExtensions.of(NReflect.class);
    }

    /**
     * Scoped bean container stack.
     *
     * @return scoped bean container stack result
     */
    NScopedStack<NBeanContainer> scopedBeanContainerStack();

    /**
     * Scoped bean container.
     *
     * @return scoped bean container result
     */
    NBeanContainer scopedBeanContainer();

    /**
     * Creates a new instance of create mapper.
     *
     * @return create mapper result
     */
    NReflectMapper createMapper();

    /**
     * Checks if is immutable type.
     *
     * @param type type
     * @return is immutable type result
     */
    boolean isImmutableType(Type type);

    /**
     * Checks if is immutable type.
     *
     * @param type type
     * @return is immutable type result
     */
    boolean isImmutableType(NReflectType type);

    /**
     * Creates a new instance of create type loader.
     *
     * @param name name
     * @return create type loader result
     */
    NTypeLoader createTypeLoader(String name);

    /**
     * Creates a new instance of platform signature.
     *
     * @param types types
     * @return of platform signature result
     */
    NPlatformSignature ofPlatformSignature(Type... types);

    /**
     * Creates a new instance of var args platform signature.
     *
     * @param types types
     * @return of var args platform signature result
     */
    NPlatformSignature ofVarArgsPlatformSignature(Type... types);

    /**
     * Creates a new instance of platform signature.
     *
     * @param name name
     * @param types types
     * @return of platform signature result
     */
    NPlatformSignature ofPlatformSignature(String name, Type... types);

    /**
     * Creates a new instance of var args platform signature.
     *
     * @param name name
     * @param types types
     * @return of var args platform signature result
     */
    NPlatformSignature ofVarArgsPlatformSignature(String name, Type... types);

    /**
     * Creates a new instance of type name signature.
     *
     * @param domain domain
     * @param types types
     * @return of type name signature result
     */
    NTypeNameSignature ofTypeNameSignature(NTypeNameDomain domain, NTypeName... types);

    /**
     * Creates a new instance of var args type name signature.
     *
     * @param domain domain
     * @param types types
     * @return of var args type name signature result
     */
    NTypeNameSignature ofVarArgsTypeNameSignature(NTypeNameDomain domain, NTypeName... types);

    /**
     * Creates a new instance of type name signature.
     *
     * @param domain domain
     * @param name name
     * @param types types
     * @return of type name signature result
     */
    NTypeNameSignature ofTypeNameSignature(NTypeNameDomain domain, String name, NTypeName... types);

    /**
     * Creates a new instance of var args type name signature.
     *
     * @param domain domain
     * @param name name
     * @param types types
     * @return of var args type name signature result
     */
    NTypeNameSignature ofVarArgsTypeNameSignature(NTypeNameDomain domain, String name, NTypeName... types);

    /**
     * Creates a new instance of reflect signature.
     *
     * @param types types
     * @return of reflect signature result
     */
    NReflectSignature ofReflectSignature(NReflectType... types);

    /**
     * Creates a new instance of var args reflect signature.
     *
     * @param types types
     * @return of var args reflect signature result
     */
    NReflectSignature ofVarArgsReflectSignature(NReflectType... types);

    /**
     * Creates a new instance of reflect signature.
     *
     * @param name name
     * @param types types
     * @return of reflect signature result
     */
    NReflectSignature ofReflectSignature(String name, NReflectType... types);

    /**
     * Creates a new instance of var args reflect signature.
     *
     * @param name name
     * @param types types
     * @return of var args reflect signature result
     */
    NReflectSignature ofVarArgsReflectSignature(String name, NReflectType... types);

    /**
     * Creates a new instance of signature map.
     *
     * @param domain domain
     * @return of signature map result
     */
    <S extends NSignature<T, ?>, T, V> NSignatureMap<S, T, V> ofSignatureMap(NSignatureDomain<T> domain);

    /**
     * Creates a new instance of platform signature map.
     *
     * @param domain domain
     * @return of platform signature map result
     */
    <V> NSignatureMap<NPlatformSignature, Type, V> ofPlatformSignatureMap(NSignatureDomain<Type> domain);

    /**
     * Creates a new instance of platform signature map.
     *
     * @return of platform signature map result
     */
    <V> NSignatureMap<NPlatformSignature, Type, V> ofPlatformSignatureMap();

    /**
     * Creates a new instance of reflect signature map.
     *
     * @return of reflect signature map result
     */
    <V> NSignatureMap<NReflectSignature, NReflectType, V> ofReflectSignatureMap();

    /**
     * Creates a new instance of t ype name signature map.
     *
     * @param domain domain
     * @return of t ype name signature map result
     */
    <V> NSignatureMap<NTypeNameSignature, NTypeName<?>, V> ofTYpeNameSignatureMap(NTypeNameDomain domain);

    /**
     * Platform domain.
     *
     * @return platform domain result
     */
    NTypeNamePlatformDomain platformDomain();

}
