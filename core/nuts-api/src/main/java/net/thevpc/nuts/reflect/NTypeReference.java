package net.thevpc.nuts.reflect;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * This is super type token implementation as described in
 * http://gafter.blogspot.com/2006/12/super-type-tokens.html
 * References a generic type.
 * @author crazybob@google.com (Bob Lee)
 */
public abstract class NTypeReference<T> implements Serializable {

    private final Type type;
    private volatile Constructor<?> constructor;

    /**
     * N type reference.
     *
     * @param type type
     * @return n type reference result
     */
    private NTypeReference(Type type) {
        this.type = type;
    }

    /**
     * N type reference.
     *
     * @return n type reference result
     */
    protected NTypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw NException.ofSafeUnexpectedException(NMsg.ofC("missing type parameter."));
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    @Override
    public String toString() {
        return "TypeReference<" + type + ">";
    }

    /**
     * Creates a new instance.
     *
     * @param type type
     * @param args args
     * @return of result
     */
    public static <T> NTypeReference<T> of(Type type, Type... args) {
        if (args != null && args.length > 0) {
            if (type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) type;
                return new NTypeReference<T>(new MyParameterizedType(ptype.getRawType(), ptype.getOwnerType(), args)) {
                };
            }
            if (type instanceof Class) {
                // of(List.class, String.class) is List<String>
                Class<?> c = (Class<?>) type;
                return new NTypeReference<T>(new MyParameterizedType(c, c.getDeclaringClass(), args)) {
                };
            }
        }
        return new NTypeReference<T>(type) {
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
            constructor = typeClass().getConstructor();
        }
        return (T) constructor.newInstance();
    }

    /**
     * Gets the referenced type.
     */
    public Class typeClass() {
        return NReflectUtils.getRawClass(type).get();
    }

    /**
     * Type.
     *
     * @return type result
     */
    public Type type() {
        return this.type;
    }

    /**
     * Checks if is assignable from.
     *
     * @param cls cls
     * @return is assignable from result
     */
    public boolean isAssignableFrom(NTypeReference<?> cls) {
        return typeClass().isAssignableFrom(cls.typeClass());
    }

    /**
     * two references are equal when they denote the same type, whatever their (usually anonymous) class is :
     * {@code new NTypeReference<List<String>>(){}} equals {@code NTypeReference.of(List.class, String.class)}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NTypeReference)) return false;

        NTypeReference<?> that = (NTypeReference<?>) o;

        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

    /**
     * Checks if is interface.
     *
     * @return is interface result
     */
    public boolean isInterface() {
        return typeClass().isInterface();
    }

    /**
     * Interfaces.
     *
     * @return interfaces result
     */
    public NTypeReference[] interfaces() {
        Class[] interfaces = typeClass().getInterfaces();
        NTypeReference[] typeReferences = new NTypeReference[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            typeReferences[i] = of(interfaces[i]);//TODO params?
        }
        return typeReferences;
    }

    /**
     * Superclass.
     *
     * @return superclass result
     */
    public NTypeReference superclass() {
        Class superclass = typeClass().getSuperclass();
        if (superclass == null) {
            return null;
        }
        return of(superclass);
    }

    /**
     * Checks if is instance.
     *
     * @param t t
     * @return is instance result
     */
    public <T> boolean isInstance(T t) {
        return typeClass().isInstance(t);
    }

    /**
     * equals/hashCode compatible with the jdk ParameterizedType implementation
     */
    private static class MyParameterizedType implements ParameterizedType, Serializable {
        private final Type rawType;
        private final Type ownerType;
        private final Type[] args;

        public MyParameterizedType(Type rawType, Type ownerType, Type... args) {
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.args = args.clone();
        }

        @Override
        public Type[] getActualTypeArguments() {
            return args.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public String getTypeName() {
            return toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(rawType.getTypeName());
            if (args.length > 0) {
                sb.append("<");
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(args[i].getTypeName());
                }
                sb.append(">");
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParameterizedType)) return false;
            ParameterizedType that = (ParameterizedType) o;
            return Objects.equals(ownerType, that.getOwnerType())
                    && Objects.equals(rawType, that.getRawType())
                    && Arrays.equals(args, that.getActualTypeArguments());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(args) ^ Objects.hashCode(ownerType) ^ Objects.hashCode(rawType);
        }
    }
}
