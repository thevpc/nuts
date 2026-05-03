package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

public interface NElementDeserializerInstanceContext<T> extends NElementFactoryContext {
    T instance();

    NElement element();

    Type instanceType();
}
