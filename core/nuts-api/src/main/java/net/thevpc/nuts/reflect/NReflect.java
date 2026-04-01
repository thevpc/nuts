package net.thevpc.nuts.reflect;

import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.lang.reflect.Type;

public interface NReflect extends NComponent {
    static NReflect of() {
        return NExtensions.of(NReflect.class);
    }

    NScopedStack<NBeanContainer> scopedBeanContainerStack();

    NBeanContainer scopedBeanContainer();

    NReflectMapper createMapper();

    boolean isImmutableType(Type type);

    boolean isImmutableType(NReflectType type);

    NTypeLoader createTypeLoader(String name);

    NPlatformSignature ofPlatformSignature(Type... types);

    NPlatformSignature ofVarArgsPlatformSignature(Type... types);

    NPlatformSignature ofPlatformSignature(String name, Type... types);

    NPlatformSignature ofVarArgsPlatformSignature(String name, Type... types);

    NTypeNameSignature ofTypeNameSignature(NTypeNameDomain domain, NTypeName... types);

    NTypeNameSignature ofVarArgsTypeNameSignature(NTypeNameDomain domain, NTypeName... types);

    NTypeNameSignature ofTypeNameSignature(NTypeNameDomain domain, String name, NTypeName... types);

    NTypeNameSignature ofVarArgsTypeNameSignature(NTypeNameDomain domain, String name, NTypeName... types);

    NReflectSignature ofReflectSignature(NReflectType... types);

    NReflectSignature ofVarArgsReflectSignature(NReflectType... types);

    NReflectSignature ofReflectSignature(String name, NReflectType... types);

    NReflectSignature ofVarArgsReflectSignature(String name, NReflectType... types);

    <S extends NSignature<T, ?>, T, V> NSignatureMap<S, T, V> ofSignatureMap(NSignatureDomain<T> domain);

    <V> NSignatureMap<NPlatformSignature, Type, V> ofPlatformSignatureMap(NSignatureDomain<Type> domain);

    <V> NSignatureMap<NPlatformSignature, Type, V> ofPlatformSignatureMap();

    <V> NSignatureMap<NReflectSignature, NReflectType, V> ofReflectSignatureMap();

    <V> NSignatureMap<NTypeNameSignature, NTypeName<?>, V> ofTYpeNameSignatureMap(NTypeNameDomain domain);

    NTypeNamePlatformDomain platformDomain();

}
