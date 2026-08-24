package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

/**
 * NElementDeserializerInstanceContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDeserializerInstanceContext<T> extends NElementFactoryContext {
    /**
     * Instance.
     *
     * @return instance result
     */
    T instance();

    /**
     * Element.
     *
     * @return element result
     */
    NElement element();

    /**
     * Instance type.
     *
     * @return instance type result
     */
    Type instanceType();
}
