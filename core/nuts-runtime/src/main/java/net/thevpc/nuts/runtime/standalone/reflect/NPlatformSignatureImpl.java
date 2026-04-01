package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.NPlatformSignature;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.runtime.standalone.reflect.NSignatureBase;
import net.thevpc.nuts.reflect.NSignatureDomain;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;

public class NPlatformSignatureImpl extends NSignatureBase<Type, NPlatformSignature> implements NPlatformSignature {
    public static final NSignatureDomain<Type> DOMAIN = new NSignatureDomain<Type>() {
        @Override
        public boolean isArray(Type type) {
            return type instanceof Class && ((Class<?>) type).isArray();
        }

        @Override
        public Type getComponentType(Type type) {
            if (type instanceof Class) {
                Class<?> c = (Class<?>) type;
                return c.getComponentType();
            }
            return null;
        }

        @Override
        public String toSignatureString(Type type) {
            if (type instanceof Class) {
                return ((Class<?>) type).getName();
            }
            return type.toString();
        }

        @Override
        public boolean isAssignableFrom(Type a, Type b) {
            if (a instanceof Class && b instanceof Class) {
                Class<?> ca = (Class<?>) a;
                Class<?> cb = (Class<?>) b;
                return ca.isAssignableFrom(cb);
            }
            return false;
        }

        @Override
        public boolean isInterface(Type any) {
            return any instanceof Class && ((Class<?>) any).isInterface();
        }

        @Override
        public Type[] getInterfaces(Type any) {
            if (any instanceof Class) {
                return ((Class<?>) any).getInterfaces();
            }
            return new Type[0];
        }

        @Override
        public Type getSuperType(Type any) {
            if (any instanceof Class) {
                return ((Class<?>) any).getSuperclass();
            }
            return null;
        }

        @Override
        public boolean isPrimitive(Type a) {
            return a instanceof Class && ((Class<?>) a).isPrimitive();
        }

        @Override
        public Type toBoxedType(Type a) {
            if (a instanceof Class) {
                Class<?> c = (Class<?>) a;
                NOptional<Class<?>> b = NReflectUtils.toBoxedType(c);
                if (b.isPresent()) {
                    return b.get();
                }
            }
            return a;
        }

        @Override
        public Type toPrimitiveType(Type a) {
            if(a instanceof Class){
                return NReflectUtils.toPrimitiveType((Class<?>) a).orNull();
            }
            return null;
        }
    };

    public static NPlatformSignature of(Type... types) {
        return new NPlatformSignatureImpl(null, types, false);
    }

    public static NPlatformSignature ofVarArgs(Type... types) {
        checkVararg(types, DOMAIN);
        return new NPlatformSignatureImpl(null, types, true);
    }

    public static NPlatformSignature of(String name, Type... types) {
        return new NPlatformSignatureImpl(name, types, false);
    }

    public static NPlatformSignature ofVarArgs(String name, Type... types) {
        checkVararg(types, DOMAIN);
        return new NPlatformSignatureImpl(name, types, true);
    }

    private NPlatformSignatureImpl(String name, Type[] types, boolean vararg) {
        super(name, types, vararg, DOMAIN);
    }

    @Override
    protected NPlatformSignature _create(String name, Type[] types, boolean vararg) {
        return new NPlatformSignatureImpl(name, types, vararg);
    }
}
