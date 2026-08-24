package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

/**
 * NElementSerializerContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementSerializerContext<T> extends NElementFactoryContext {
    /**
     * Instance.
     *
     * @return instance result
     */
    T instance();

    /**
     * Instance type.
     *
     * @return instance type result
     */
    Type instanceType();
}
