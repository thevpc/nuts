package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

public interface NElementDeserializerContext extends NElementFactoryContext {
    NElement element();

    Type instanceType();
}
