package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

@FunctionalInterface
public interface NElementDeserializer<T> {

    T createObject(NElement o, Type typeOfResult, NElementFactoryContext context);
}
