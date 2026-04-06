package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.runtime.standalone.reflect.mapper.NReflectMapperImpl;
import net.thevpc.nuts.runtime.standalone.reflect.mapper.TypeHelper;
import net.thevpc.nuts.runtime.standalone.util.NTypeLoaderImpl;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NImmutable;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.temporal.Temporal;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NReflectImpl implements NReflect {
    public static NTypeNamePlatformDomain PLATFORM_DOMAIN = new JavaNTypeNameDomain();

    @Override
    public NScopedStack<NBeanContainer> scopedBeanContainerStack() {
        return NWorkspaceExt.of().getModel().scopedBeanContainerStack;
    }

    @Override
    public NBeanContainer scopedBeanContainer() {
        return NWorkspaceExt.of().getModel().scopedBeanContainer;
    }

    public NReflectMapper createMapper() {
        return new NReflectMapperImpl(null);
    }

    @Override
    public boolean isImmutableType(Type type) {
        if (type == null) {
            return false;
        }

        // Primitives and boxed types
        if (TypeHelper.isBoxedOrPrimitive(type)) {
            return true;
        }

        // String
        if (String.class.equals(type)) {
            return true;
        }

        // Numbers
        if (Number.class.isAssignableFrom(TypeHelper.toClass(type))) {
            return true;
        }

        // Enums
        if (TypeHelper.toClass(type).isEnum()) {
            return true;
        }

        // Temporal types (LocalDate, Instant, Duration, etc.)
        if (TypeHelper.isAssignableFrom(Temporal.class, type)) {
            return true;
        }

        // Common immutable Java classes
        Class<?> cls = TypeHelper.toClass(type);
        if (cls == URI.class
                || cls == UUID.class
                || cls == Locale.class
                || cls == Currency.class
                || cls == Class.class
                || cls == StackTraceElement.class) {
            return true;
        }

        // Classes annotated with @NImmutable
        return cls.getAnnotation(NImmutable.class) != null;

        // By default, not immutable
    }

    @Override
    public boolean isImmutableType(NReflectType type) {
        Type t = type.getJavaType();
        if (t != null) {
            return isImmutableType(t);
        }
        return false;
    }

    @Override
    public NTypeLoader createTypeLoader(String name) {
        return new NTypeLoaderImpl(name);
    }


    @Override
    public NPlatformSignature ofPlatformSignature(Type... types) {
        return NPlatformSignatureImpl.of(types);
    }

    @Override
    public NPlatformSignature ofVarArgsPlatformSignature(Type... types) {
        return NPlatformSignatureImpl.ofVarArgs(types);
    }

    @Override
    public NPlatformSignature ofPlatformSignature(String name, Type... types) {
        return NPlatformSignatureImpl.of(name, types);
    }

    @Override
    public NPlatformSignature ofVarArgsPlatformSignature(String name, Type... types) {
        return NPlatformSignatureImpl.ofVarArgs(name, types);
    }

    /// ////////

    @Override
    public NTypeNameSignature ofTypeNameSignature(NTypeNameDomain domain, NTypeName... types) {
        return NTypeNameSignatureImpl.of(domain, types);
    }

    @Override
    public NTypeNameSignature ofVarArgsTypeNameSignature(NTypeNameDomain domain, NTypeName... types) {
        return NTypeNameSignatureImpl.ofVarArgs(domain, types);
    }

    @Override
    public NTypeNameSignature ofTypeNameSignature(NTypeNameDomain domain, String name, NTypeName... types) {
        return NTypeNameSignatureImpl.of(domain, name, types);
    }

    @Override
    public NTypeNameSignature ofVarArgsTypeNameSignature(NTypeNameDomain domain, String name, NTypeName... types) {
        return NTypeNameSignatureImpl.ofVarArgs(domain, name, types);
    }

    /// ////////

    @Override
    public NReflectSignature ofReflectSignature(NReflectType... types) {
        return NReflectSignatureImpl.of(types);
    }

    @Override
    public NReflectSignature ofVarArgsReflectSignature(NReflectType... types) {
        return NReflectSignatureImpl.ofVarArgs(types);
    }

    @Override
    public NReflectSignature ofReflectSignature(String name, NReflectType... types) {
        return NReflectSignatureImpl.of(name, types);
    }

    @Override
    public NReflectSignature ofVarArgsReflectSignature(String name, NReflectType... types) {
        return NReflectSignatureImpl.ofVarArgs(name, types);
    }


    @Override
    public <S extends NSignature<T, ?>, T, V> NSignatureMap<S, T, V> ofSignatureMap(NSignatureDomain<T> domain) {
        return new NSignatureMapImpl<>(domain);
    }

    @Override
    public <V> NSignatureMap<NPlatformSignature, Type, V> ofPlatformSignatureMap(NSignatureDomain<Type> domain) {
        return new NSignatureMapImpl<>(domain);
    }

    @Override
    public <V> NSignatureMap<NPlatformSignature, Type, V> ofPlatformSignatureMap() {
        return new NSignatureMapImpl<>(NPlatformSignatureImpl.DOMAIN);
    }

    @Override
    public <V> NSignatureMap<NReflectSignature, NReflectType, V> ofReflectSignatureMap() {
        return new NSignatureMapImpl<>(NReflectSignatureImpl.DOMAIN);
    }

    @Override
    public <V> NSignatureMap<NTypeNameSignature, NTypeName<?>, V> ofTYpeNameSignatureMap(NTypeNameDomain domain) {
        return new NSignatureMapImpl<NTypeNameSignature, NTypeName<?>, V>(domain);
    }

    @Override
    public NTypeNamePlatformDomain platformDomain() {
        return PLATFORM_DOMAIN;
    }
}
