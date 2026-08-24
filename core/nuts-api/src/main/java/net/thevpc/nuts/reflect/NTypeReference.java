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
            /**
             * Runtime exception.
             *
             * @param parameter." parameter."
             * @return runtime exception result
             */
            throw new RuntimeException("missing type parameter.");
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    @Override
    public String toString() {
        return "TypeReference<" + type + ">";
    }

    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @param args args
     * @return of result
     */
    public static <T> NTypeReference<T> of(Type type, Type... args) {
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            ParameterizedType ptype2 = new MyParameterizedType(ptype, args);
            return new NTypeReference<T>(ptype2) {
            };
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
            Class<?> rawType = type instanceof Class<?>
                    ? (Class<?>) type
                    : (Class<?>) ((ParameterizedType) type).getRawType();
            constructor = rawType.getConstructor();
        }
      /**
       * Return.
       *
       * @param constructor.newInstance( constructor.new instance(
       */
        return (T) constructor.newInstance();
    }

    /**
     * Gets the referenced type.
     */
    public Class typeClass() {
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
        /**
         * Type class.
         *
         * @param ).isAssignableFrom(cls.typeClass() ).is assignable from(cls.type class()
         * @return type class result
         */
        return typeClass().isAssignableFrom(cls.typeClass());
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

    /**
     * Checks if is interface.
     *
     * @return is interface result
     */
    public boolean isInterface() {
        /**
         * Type class.
         *
         * @param ).isInterface( ).is interface(
         * @return type class result
         */
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
        /**
         * Creates a new instance of of.
         *
         * @param superclass superclass
         * @return of result
         */
        return of(superclass);
    }

    /**
     * Checks if is instance.
     *
     * @param t t
     * @return is instance result
     */
    public <T> boolean isInstance(T t) {
        /**
         * Type class.
         *
         * @param ).isInstance(t ).is instance(t
         * @return type class result
         */
        return typeClass().isInstance(t);
    }

    private static class MyParameterizedType implements ParameterizedType {
        private final ParameterizedType ptype;
        private final Type[] args;

        /**
         * My parameterized type.
         *
         * @param ptype ptype
         * @param args args
         * @return my parameterized type result
         */
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
