package net.thevpc.nuts.reflect;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NAssert;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

public class NBeanRef {
    private String id;
    private NElement variant;

    public static NBeanRef of(String id, NElement variant) {
        return new NBeanRef(id, variant);
    }

    public static NBeanRef of(String id) {
        return new NBeanRef(id, NElement.ofNull());
    }

    public NBeanRef(String id, NElement variant) {
        this.id = id;
        this.variant = variant;
    }

    public String getId() {
        return id;
    }

    public NElement getVariant() {
        return variant;
    }

    public <T> T as(Class<T> type) {
        NAssert.requireNonNull(type, "type");
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type, NBeanRefHolder.class},
                new BeanInvocationHandler(this)
        );
    }

    interface NBeanRefHolder {
        NBeanRef asBeanRef();
    }

    private static class BeanInvocationHandler implements InvocationHandler, Serializable {
        private final NBeanRef ref;

        public BeanInvocationHandler(NBeanRef ref) {
            this.ref = ref;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("asBeanRef") && method.getParameterCount() == 0) {
                return ref;
            }
            NBeanContainer container = detectContainer();
            NAssert.requireNonNull(container, "bean container");
            Object bean = container.of(ref); // resolves actual bean
            return method.invoke(bean, args); // delegate call
        }

        private NBeanContainer detectContainer() {
            return NBeanContainer.current().get();
        }

        @Override
        public String toString() {
            return "NBeanRef{id=" + ref.getId() + ", variant=" + ref.getVariant() + "}";
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
