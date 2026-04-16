package net.thevpc.nuts.reflect;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * This is super type token implementation as described in
 * http://gafter.blogspot.com/2006/12/super-type-tokens.html
 * References a generic type.
 * @author crazybob@google.com (Bob Lee)
 */
public abstract class NTypeReference<T> implements Serializable {

    private final Type type;
    private volatile Constructor<?> constructor;

    private NTypeReference(Type type) {
        this.type = type;
    }

    protected NTypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("missing type parameter.");
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    @Override
    public String toString() {
        return "TypeReference<" + type + ">";
    }

    public static NTypeReference of(Type type, Type... args) {
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            ParameterizedType ptype2 = new MyParameterizedType(ptype, args);
            return new NTypeReference(ptype2) {
            };
        }
        return new NTypeReference(type) {
        };
    }

    /**
     * Instantiates a new instance of {@code T} using the default, no-arg
     * constructor.
     */
    @SuppressWarnings("unchecked")
    public T newInstance()
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        if (constructor == null) {
            Class<?> rawType = type instanceof Class<?>
                    ? (Class<?>) type
                    : (Class<?>) ((ParameterizedType) type).getRawType();
            constructor = rawType.getConstructor();
        }
        return (T) constructor.newInstance();
    }

    /**
     * Gets the referenced type.
     */
    public Class getTypeClass() {
        try {
            Type tt = type;
            while (tt instanceof ParameterizedType) {
                tt = ((ParameterizedType) tt).getRawType();
            }
            return (Class) tt;
        } catch (ClassCastException ex) {
            throw ex;
        }
    }

    public Type getType() {
        return this.type;
    }

    public boolean isAssignableFrom(NTypeReference<?> cls) {
        return getTypeClass().isAssignableFrom(cls.getTypeClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NTypeReference<?> that = (NTypeReference<?>) o;

        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

    public boolean isInterface() {
        return getTypeClass().isInterface();
    }

    public NTypeReference[] getInterfaces() {
        Class[] interfaces = getTypeClass().getInterfaces();
        NTypeReference[] typeReferences = new NTypeReference[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            typeReferences[i] = of(interfaces[i]);//TODO params?
        }
        return typeReferences;
    }

    public NTypeReference getSuperclass() {
        Class superclass = getTypeClass().getSuperclass();
        if (superclass == null) {
            return null;
        }
        return of(superclass);
    }

    public <T> boolean isInstance(T t) {
        return getTypeClass().isInstance(t);
    }

    private static class MyParameterizedType implements ParameterizedType {
        private final ParameterizedType ptype;
        private final Type[] args;

        public MyParameterizedType(ParameterizedType ptype, Type... args) {
            this.ptype = ptype;
            this.args = args;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return ptype.getRawType();
        }

        @Override
        public Type getOwnerType() {
            return ptype.getOwnerType();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyParameterizedType that = (MyParameterizedType) o;

            if (ptype != null ? !ptype.equals(that.ptype) : that.ptype != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(args, that.args);
        }

        @Override
        public int hashCode() {
            int result = ptype != null ? ptype.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(args);
            return result;
        }
    }
}
