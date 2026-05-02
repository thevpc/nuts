package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

@FunctionalInterface
public interface NElementSerializer<T> {
    NElement createElement(T src, Type typeOfSrc, NElementFactoryContext context);
}
