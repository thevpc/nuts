package net.thevpc.nuts.reflect;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NAssert;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * NBeanRef class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NBeanRef {
    private String id;
    private NElement variant;

    /**
     * Creates a new instance of of.
     *
     * @param id id
     * @param variant variant
     * @return of result
     */
    public static NBeanRef of(String id, NElement variant) {
        return new NBeanRef(id, variant);
    }

    /**
     * Creates a new instance of of.
     *
     * @param id id
     * @return of result
     */
    public static NBeanRef of(String id) {
        return new NBeanRef(id, NElement.ofNull());
    }

    /**
     * N bean ref.
     *
     * @param id id
     * @param variant variant
     * @return n bean ref result
     */
    public NBeanRef(String id, NElement variant) {
        this.id = id;
        this.variant = variant;
    }

    /**
     * Id.
     *
     * @return id result
     */
    public String id() {
        return id;
    }

    /**
     * Variant.
     *
     * @return variant result
     */
    public NElement variant() {
        return variant;
    }

    /**
     * As.
     *
     * @param type type
     * @return as result
     */
    public <T> T as(Class<T> type) {
        NAssert.requireNamedNonNull(type, "type");
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type, NBeanRefHolder.class},
                new BeanInvocationHandler(this)
        );
    }

    interface NBeanRefHolder {
        /**
         * As bean ref.
         *
         * @return as bean ref result
         */
        NBeanRef asBeanRef();
    }

    private static class BeanInvocationHandler implements InvocationHandler, Serializable {
        private final NBeanRef ref;

        /**
         * Bean invocation handler.
         *
         * @param ref ref
         * @return bean invocation handler result
         */
        public BeanInvocationHandler(NBeanRef ref) {
            this.ref = ref;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("asBeanRef") && method.getParameterCount() == 0) {
                return ref;
            }
            Object bean = NBeanContainer.current().of(ref); // resolves actual bean
            return method.invoke(bean, args); // delegate call
        }

        @Override
        public String toString() {
            return "NBeanRef{id=" + ref.id() + ", variant=" + ref.variant() + "}";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NBeanRef nBeanRef = (NBeanRef) o;
        return Objects.equals(id, nBeanRef.id) && Objects.equals(variant, nBeanRef.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, variant);
    }

    @Override
    public String toString() {
        return "NBeanRef{id=" + id + ", variant=" + variant + "}";
    }
}
