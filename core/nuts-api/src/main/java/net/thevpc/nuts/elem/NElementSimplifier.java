package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

@FunctionalInterface
public interface NElementSimplifier<T> {
    Object toSimple(T src, Type typeOfSrc, NElementFactoryContext context);
}
