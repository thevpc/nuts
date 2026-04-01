package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.reflect.NTypeName;
import net.thevpc.nuts.reflect.NTypeNamePlatformDomain;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;

class JavaNTypeNameDomain implements NTypeNamePlatformDomain {
    @Override
    public boolean isInterface(NTypeName any) {
        return getTypeClass(any).isInterface();
    }

    public <A> Class<A> getTypeClass(NTypeName<A> any) {
        return toCls(any.name(), null);
    }

    private Class toCls(String typeName, ClassLoader cl) {
        try {
            return Class.forName(typeName, true, cl == null ? Thread.currentThread().getContextClassLoader() : cl);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(typeName);
            } catch (ClassNotFoundException e2) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @Override
    public boolean isArray(NTypeName type) {
        return type.isArray();
    }

    @Override
    public NTypeName getComponentType(NTypeName type) {
        return type.getComponentType();
    }

    @Override
    public String toSignatureString(NTypeName type) {
        return type.toString();
    }

    @Override
    public boolean isPrimitive(NTypeName a) {
        Class c = getTypeClass(a);
        return c != null && c.isPrimitive();
    }

    @Override
    public NTypeName toBoxedType(NTypeName a) {
        Class c = getTypeClass(a);
        if (c != null) {
            NOptional<Class<?>> b = NReflectUtils.toBoxedType(c);
            if (b.isPresent()) {
                return NTypeName.of(b.get());
            }
        }
        return a;
    }

    @Override
    public NTypeName toPrimitiveType(NTypeName a) {
        Class c = getTypeClass(a);
        if (c != null) {
            Class<?> o = NReflectUtils.toPrimitiveType((Class<?>) c).orNull();
            if(o!=null){
                return NTypeName.of(o);
            }
        }
        return null;
    }

    @Override
    public boolean isAssignableFrom(NTypeName a, NTypeName b) {
        Type aa = getTypeClass(a);
        Type bb = getTypeClass(b);
        if (aa instanceof Class && bb instanceof Class) {
            Class<?> ca = (Class<?>) aa;
            Class<?> cb = (Class<?>) bb;
            return ca.isAssignableFrom(cb);
        }
        return false;
    }


    @Override
    public NTypeName[] getInterfaces(NTypeName any) {
        Class[] interfaces = getTypeClass(any).getInterfaces();
        NTypeName[] typeReferences = new NTypeName[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            typeReferences[i] = NTypeName.of(interfaces[i]);//TODO params?
        }
        return typeReferences;
    }

    @Override
    public NTypeName<?> getSuperType(NTypeName any) {
        Class superclass = getTypeClass(any).getSuperclass();
        if (superclass == null) {
            return null;
        }
        return NTypeName.of(superclass);
    }
}
