package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.runtime.standalone.reflect.mapper.NReflectMapperImpl;
import net.thevpc.nuts.runtime.standalone.reflect.mapper.TypeHelper;
import net.thevpc.nuts.runtime.standalone.reflect.mapper.TypeMapperRepositoryDef;
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
        if (cls.getAnnotation(NImmutable.class) != null) {
            return true;
        }

        // By default, not immutable
        return false;
    }

    @Override
    public boolean isImmutableType(NReflectType type) {
        Type t = type.getJavaType();
        if (t != null) {
            return isImmutableType(t);
        }
        return false;
    }
}
