package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

/**
 * NElementDeserializerContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDeserializerContext extends NElementFactoryContext {
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
