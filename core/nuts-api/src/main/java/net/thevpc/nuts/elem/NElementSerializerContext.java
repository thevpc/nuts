package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

public interface NElementSerializerContext<T> extends NElementFactoryContext {
    T instance();

    Type instanceType();
}
